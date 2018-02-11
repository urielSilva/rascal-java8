package com.rascaljava;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;

import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class RascalJavaInterface {
	private final IValueFactory vf;

    public RascalJavaInterface(IValueFactory vf) {
       this.vf = vf;
    }
    public IValue testeJava() {
        return vf.integer(1);
    }
    
    public static void main(String[] args) {
    		List<File> result = IOUtil.findAllFiles("/Users/uriel/Documents/Projetos/poup/poupweb/src", "java");
    		CompilationUnitProcessor processor = new CompilationUnitProcessor();
    		List<CompilationUnit> compiledFiles = result.stream().map(CompilationUnitProcessor::getCompilationUnit).collect(Collectors.toList());
    		compiledFiles.forEach((cu) -> { processor.setCompilationUnit(cu); processor.processCompilationUnit();});
    		processor.fetchFromDb();
	}
    
    
}
