package com.rascaljava;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class RascalJavaInterface {
	private final IValueFactory vf;

    public RascalJavaInterface(IValueFactory vf) {
       this.vf = vf;
    }
    
    public void initDB(IString projectPath) {
    	DB.getInstance().setup();
    	populateDb(projectPath.getValue());
    }
    
    public IValue testeJava(IString exc) {
    	return vf.bool(isCheckedException(exc.getValue()));
    }
    
    public static void populateDb(String projectPath) {
    	List<File> result = IOUtil.findAllFiles(projectPath, "java");
		CompilationUnitProcessor processor = new CompilationUnitProcessor();
		List<CompilationUnit> compiledFiles = result.stream().map(CompilationUnitProcessor::getCompilationUnit).collect(Collectors.toList());
		compiledFiles.forEach((cu) -> { processor.setCompilationUnit(cu); processor.processCompilationUnit();});
    }
    
    public static void main(String[] args) {
    	DB.getInstance().setup();
		populateDb("/Users/uriel/Documents/Projetos/poup/poupweb/src");
		Integer count = DB.getInstance().fetchFromDb();
		System.out.println("count: " + count);
		System.out.println(isCheckedException("IOException"));
	}
    
    public IValue isCheckedException(IString exc) {
		String exception = DB.getInstance().findQualifiedName(exc.getValue());
		if(exception != null) {
			CombinedTypeSolver solver = new CombinedTypeSolver();
			solver.add(new JavaParserTypeSolver(new File("/Users/uriel/Documents/Projetos/poup/poupweb/src")));
			solver.add(new ReflectionTypeSolver());
			ResolvedReferenceTypeDeclaration res = solver.solveType(exception);
			return vf.bool(res.getAllAncestors().stream().anyMatch((a) -> a.getQualifiedName().equalsIgnoreCase("java.lang.Exception")));
		}
		return vf.bool(false);
    }
    
    public static boolean isCheckedException(String exc) {
		String exception = DB.getInstance().findQualifiedName(exc);
		if(exception != null) {
			CombinedTypeSolver solver = new CombinedTypeSolver();
			solver.add(new JavaParserTypeSolver(new File("/Users/uriel/Documents/Projetos/poup/poupweb/src")));
			solver.add(new ReflectionTypeSolver());
			ResolvedReferenceTypeDeclaration res = solver.solveType(exception);
			return res.getAllAncestors().stream().anyMatch((a) -> a.getQualifiedName().equalsIgnoreCase("java.lang.Exception"));
		}
		return false;
    }
}
