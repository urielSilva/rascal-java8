package com.rascaljava;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
import com.google.gson.Gson;
import java.sql.*;

public class CompilationUnitProcessor {

	private CompilationUnit compilationUnit;
	
	private Connection connection;
	public CompilationUnitProcessor() {
		setupDb();
	}

	public void processCompilationUnit() {
		ClassDefinition classDef = new ClassDefinition();
		processClassInformation(classDef);
		processMethodsInformation(classDef);
		processFieldsInformation(classDef);
//		System.out.println(new Gson().toJson(classDef));
		saveToDb(classDef);
	}

	private void setupDb() {
		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.
			        getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
			PreparedStatement stmt = connection.prepareStatement("CREATE TABLE CLASS_DEFINITION(content CLOB)");
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection() {
		return connection;
	}

	public void saveToDb(ClassDefinition classDef) {
	    try {
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO CLASS_DEFINITION values(?)");
			stmt.setString(1, new Gson().toJson(classDef));
			stmt.executeUpdate();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void fetchFromDb() {
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM CLASS_DEFINITION");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				System.out.println("count: " + rs.getInt(1));
			}
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			classDef.addMethodDefinition(m.getNameAsString(), m.getType().toString(), args);
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
