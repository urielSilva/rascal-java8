package com.mypackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class Teste {
	private final IValueFactory vf;

    public Teste(IValueFactory vf) {
       this.vf = vf;
    }
    public IValue testeJava() {
        return vf.integer(1);
    }
    
    public static void main(String[] args) {
    		List<File> result = findAllFiles("/Users/uriel/Documents/Projetos/poup/poupweb/src", "java");
    		List<CompilationUnit> compiledFiles = result.stream().map(Teste::getCompilationUnit).collect(Collectors.toList());
    		compiledFiles.forEach((cu) -> processCompilationUnit(cu));
    		
	}
    
    public static void processCompilationUnit(CompilationUnit cu) {
    		processClassInformation(cu);
//	    	CompilationUnit cu = compiledFiles
//	    			.stream()
//	    			.filter((c) -> c.findFirst(ClassOrInterfaceDeclaration.class).isPresent())
//	    			.findFirst().get();
//	    		System.out.println(cu.getPackageDeclaration().get());
//	    		System.out.println(cu.findFirst(ClassOrInterfaceDeclaration.class).get().getName());
    }
    
    public static void processClassInformation(CompilationUnit cu) {
    		Optional<ClassOrInterfaceDeclaration> opClassDec = cu.findFirst(ClassOrInterfaceDeclaration.class);
    		if (!opClassDec.isPresent()) return;
    		ClassOrInterfaceDeclaration classDec = opClassDec.get();
    		System.out.println("Qualified name: " + getPackage(cu) + classDec.getName());
    }
    
    public static String getPackage(CompilationUnit cu) {
    		return cu.getPackageDeclaration().get().getNameAsString();
    }
    public static CompilationUnit getCompilationUnit(File file) {
    		try {
			return JavaParser.parse(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}	
    }
    
    public static List<File> findAllFiles(String location, String extension) {
    	  List<File> res = new ArrayList<>();
    	  List<File> allFiles = new ArrayList<>(); 
    	  File root = new File(location);
    	  if(root.isDirectory() || (getFileExtension(root).equals("jar")) || (getFileExtension(root).equals("zip"))) {
    	     allFiles.addAll(Arrays.asList(root.listFiles()));
    	  }
    	  else {
    	    allFiles.add(root);
    	  }
    	  
    	  for(File f : allFiles) {
    	    if(f.isDirectory()) {
    	      res.addAll(findAllFiles(f.getPath(), "java"));
    	    }
    	    else {
    	      if(getFileExtension(f).equals(extension)) {
    	         res.add(f);
    	      };
    	    };
    	  };
    	  return res; 
    	}
    
    public static String getFileExtension(File file) {
    		String fileName = file.getName();
    		if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
    			return fileName.substring(fileName.lastIndexOf(".")+1);
    		} else {
    			return "";
    		}
    }
}
