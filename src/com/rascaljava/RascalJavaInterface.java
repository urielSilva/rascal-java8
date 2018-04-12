package com.rascaljava;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
//    	DB.getInstance().setup();
//		populateDb("/Users/uriel/Documents/Projetos/poup/poupweb/src");
//		Integer count = DB.getInstance().fetchFromDb();
//		System.out.println("count: " + count);
//		System.out.println(isCheckedException("IOException"));
    	StringBuffer output = new StringBuffer();
    	Runtime rt = Runtime.getRuntime();
    	try {
    		ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/mvn","dependency:copy-dependencies", "-DoutputDirectory=dependencies", "-DoverWriteSnapshots=true", "-DoverWriteReleases=false");
    		pb.directory(new File("/Users/uriel/Documents/Projetos/mavenproject"));
    		Process pr = pb.start();
			pr.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(pr.getInputStream()));

                        String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println(output.toString());

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
