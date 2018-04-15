package com.rascaljava;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
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
    
    public IValue initDB(IString projectPath) {
    	DB.getInstance().setup();
    	populateDb(projectPath.getValue());
    	return vf.integer(DB.getInstance().countInserted());
    }
    
    public void initDB(String projectPath) {
    	DB.getInstance().setup();
    	populateDb(projectPath);
    	System.out.println(DB.getInstance().countInserted());
    }
    
    public void populateDb(String projectPath) {
		CombinedTypeSolver typeSolver = getTypeSolver(projectPath);
		CompilationUnitProcessor processor = new CompilationUnitProcessor(typeSolver);
		populateNativeExceptions(processor, typeSolver);
		
		
		List<File> result = IOUtil.findAllFiles(projectPath + "/src/main", "java");
		List<CompilationUnit> compiledFiles = result.stream().map(CompilationUnitProcessor::getCompilationUnit).collect(Collectors.toList());
		compiledFiles.forEach((cu) -> { processor.setCompilationUnit(cu); processor.processCompilationUnit();});
    }
    
    public void populateNativeExceptions(CompilationUnitProcessor processor, CombinedTypeSolver solver) {
    	processor.processClass(solver.solveType("java.io.IOException"));
    	processor.processClass(solver.solveType("java.lang.Exception"));
    }

	public static CombinedTypeSolver getTypeSolver(String projectPath) {
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

	public static void copyProjectJars(String projectPath) {
		System.out.println("Starting JARs download. at " + new Timestamp(System.currentTimeMillis()));
    	try {
    		ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/mvn","dependency:copy-dependencies", "-DoutputDirectory=dependencies", "-DoverWriteSnapshots=true", "-DoverWriteReleases=false");
    		pb.directory(new File(projectPath));
    		Process pr = pb.start();
			pr.waitFor();
			System.out.println("JARs downloaded successfully at "  + new Timestamp(System.currentTimeMillis()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new RascalJavaInterface(null).initDB("/Users/uriel/Documents/Projetos/pessoal/sistemaTG");
		DB.getInstance().fetchFromDb();
		System.out.println(isRelated("Exception", "IOException"));
	}
    
//    public IValue isCheckedException(IString exc) {
//		String exception = DB.getInstance().findQualifiedName(exc.getValue());
//		if(exception != null) {
//			CombinedTypeSolver solver = new CombinedTypeSolver();
//			solver.add(new JavaParserTypeSolver(new File("/Users/uriel/Documents/Projetos/poup/poupweb/src")));
//			solver.add(new ReflectionTypeSolver());
//			ResolvedReferenceTypeDeclaration res = solver.solveType(exception);
//			return vf.bool(res.getAllAncestors().stream().anyMatch((a) -> a.getQualifiedName().equalsIgnoreCase("java.lang.Exception")));
//		}
//		return vf.bool(false);
//    }
//    
    public IValue isRelated(IString clazzA, IString clazzB) {
    	String qualifiedClazzA = DB.getInstance().findQualifiedName(clazzA.getValue().trim());
    	String qualifiedClazzB = DB.getInstance().findQualifiedName(clazzB.getValue().trim());
    	System.out.println(qualifiedClazzA);
    	System.out.println(qualifiedClazzB);
    	List<ClassDefinition> classAAncestors = DB.getInstance().getAllAncestors(qualifiedClazzA);
    	List<ClassDefinition> classBAncestors = DB.getInstance().getAllAncestors(qualifiedClazzB);
    	return vf.bool(classAAncestors.stream().anyMatch((a) -> a.getQualifiedName().equals(qualifiedClazzB)) || 
    			classBAncestors.stream().anyMatch((a) -> a.getQualifiedName().equals(qualifiedClazzA)));

    }
    
    public IValue getQualifiedName(IString clazz) {
    	return vf.string(DB.getInstance().findQualifiedName(clazz.getValue().trim()));
    }
    
    public static String getQualifiedName(String clazz) {
    	return DB.getInstance().findQualifiedName("TesteException");
    }

    public static boolean isRelated(String clazzA, String clazzB) {
    	String qualifiedClazzA = DB.getInstance().findQualifiedName(clazzA);
    	String qualifiedClazzB = DB.getInstance().findQualifiedName(clazzB);
    	System.out.println(qualifiedClazzA);
    	System.out.println(qualifiedClazzB);
    	List<ClassDefinition> classAAncestors = DB.getInstance().getAllAncestors(qualifiedClazzA);
    	List<ClassDefinition> classBAncestors = DB.getInstance().getAllAncestors(qualifiedClazzB);
    	return classAAncestors.stream().anyMatch((a) -> a.getQualifiedName().equals(qualifiedClazzB)) || 
    			classBAncestors.stream().anyMatch((a) -> a.getQualifiedName().equals(qualifiedClazzA));
    }
}
