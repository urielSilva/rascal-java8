package com.rascaljava;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.resolution.SymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
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
    
    public void initDB(String projectPath) {
    	DB.getInstance().setup();
    	populateDb(projectPath);
    }
    
    public IValue testeJava(IString exc) {
    	return vf.bool(isCheckedException(exc.getValue()));
    }
    
    public void populateDb(String projectPath) {
    	List<File> result = IOUtil.findAllFiles(projectPath + "/src/main", "java");
		CompilationUnitProcessor processor = new CompilationUnitProcessor(projectPath, getTypeSolver(projectPath));
		List<CompilationUnit> compiledFiles = result.stream().map(CompilationUnitProcessor::getCompilationUnit).collect(Collectors.toList());
		compiledFiles.forEach((cu) -> { processor.setCompilationUnit(cu); processor.processCompilationUnit();});
    }
    
    private static CombinedTypeSolver getTypeSolver(String projectPath) {
		CombinedTypeSolver solver;
		solver = new CombinedTypeSolver();
		solver.add(new JavaParserTypeSolver(new File(projectPath + "/src/main/java")));
		solver.add(new ReflectionTypeSolver());
		copyProjectJars(projectPath);
		List<File> jars = IOUtil.findAllFiles(projectPath + "/dependencies", "jar");
		jars.forEach((jar) -> {
			try {
				solver.add(new JarTypeSolver(jar.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return solver;
	}

	private static void copyProjectJars(String projectPath) {
		StringBuffer output = new StringBuffer();
    	Runtime rt = Runtime.getRuntime();
    	try {
    		ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/mvn","dependency:copy-dependencies", "-DoutputDirectory=dependencies", "-DoverWriteSnapshots=true", "-DoverWriteReleases=false");
    		pb.directory(new File(projectPath));
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
	}

	public static void main(String[] args) {
		new RascalJavaInterface(null).initDB("/Users/uriel/Documents/Projetos/mavenproject");
//		DB.getInstance().fetchFromDb();
		System.out.println(isSuperClass("Teste", "App"));
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
//		String exception = DB.getInstance().findQualifiedName(exc);
//		if(exception != null) {
			CombinedTypeSolver solver = new CombinedTypeSolver();
			solver.add(new JavaParserTypeSolver(new File("/Users/uriel/Documents/Projetos/mavenproject/src/main")));
			solver.add(new ReflectionTypeSolver());
			ResolvedReferenceTypeDeclaration res = solver.solveType("org.teste.mavenproject.App");
			System.out.println(res.getQualifiedName());
//		}
		return false;
    }
    
    public static boolean isSuperClass(String clazz, String superClazz) {
    	String qualifiedClazz = DB.getInstance().findQualifiedName(clazz);
    	String qualifiedSuperClazz = DB.getInstance().findQualifiedName(superClazz);
    	List<ClassDefinition> allAncestors = DB.getInstance().getAllAncestors(qualifiedClazz);
    	return allAncestors.stream().anyMatch((a) -> a.getQualifiedName().equals(qualifiedSuperClazz));
    }
}
