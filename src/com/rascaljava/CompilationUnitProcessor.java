package com.rascaljava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.common.collect.Multiset.Entry;

public class CompilationUnitProcessor {

	private CompilationUnit compilationUnit;
	
	private DB dbConnection;
	
	public CompilationUnitProcessor() {
		dbConnection = DB.getInstance();
	}

	public void processCompilationUnit() {
		ClassDefinition classDef = new ClassDefinition();
		processClassInformation(classDef);
		processMethodsInformation(classDef);
		processFieldsInformation(classDef);
		dbConnection.saveToDb(classDef);
	}

	
	public void processClassInformation(ClassDefinition classDef) {
		Optional<ClassOrInterfaceDeclaration> opClassDec = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class);
		if (!opClassDec.isPresent()) return;
		ClassOrInterfaceDeclaration classDec = opClassDec.get();
		classDef.setQualifiedName(getPackage() + "." + classDec.getName());
		classDef.setExtendedTypes(
			classDec
			.getExtendedTypes()
			.stream()
			.map(ClassOrInterfaceType::getNameAsString)
			.collect(Collectors.toList()));
		

		classDef.setImplementedTypes(
				classDec
				.getImplementedTypes()
				.stream()
				.map(ClassOrInterfaceType::getNameAsString)
				.collect(Collectors.toList()));

		classDef.setAnnotations(
				classDec
				.getAnnotations()
				.stream()
				.map(AnnotationExpr::getNameAsString)
				.collect(Collectors.toList()));
	}

	public void processMethodsInformation(ClassDefinition classDef) {
		compilationUnit.findAll(MethodDeclaration.class).forEach((m) -> {
			Map<String, String> args = m.getParameters().stream()
					.collect(Collectors.toMap((p) -> p.getNameAsString(), (p) -> p.getType().toString()));
	    		
			List<ClassDefinition> exceptions = m
					.getThrownExceptions()
					.stream()
					.map((e) -> new ClassDefinition(e.findCompilationUnit().get().getPackageDeclaration().get().getNameAsString() + "." + e.toString()))
					.collect(Collectors.toList());
			classDef.addMethodDefinition(m.getNameAsString(), m.getType().toString(), args, exceptions );
		});
	}
	
	public void processFieldsInformation(ClassDefinition classDef) {
		compilationUnit.findAll(FieldDeclaration.class).forEach((f) -> {
			f.getVariables().forEach((v) -> classDef.addFieldDefinition(v.getNameAsString(), v.getType().toString()));
		});
	}

	public String getPackage() {
		return compilationUnit.getPackageDeclaration().get().getNameAsString();
	}

	public static CompilationUnit getCompilationUnit(File file) {
		try {
			return JavaParser.parse(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}
}
