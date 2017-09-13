module lang::java::refactoring::JUnitClassDeclaration

import ParseTree;
import lang::java::\syntax::Java18;
import IO;


/**
 * Refactor a compilation unit to replace 
 * a JUnit 3 class declaration for a JUnit 4 one
 */ 
public tuple[int, CompilationUnit] refactorJUnitClassDeclaration(CompilationUnit cu) {
   int total = 0;
   println(cu); 

   CompilationUnit unit =  visit(cu) {
       case(NormalClassDeclaration)`public class <Identifier clazz> extends TestCase {<ClassBodyDeclaration* dec>}`: {
      	  total += 1;
          insert (NormalClassDeclaration)`public class <Identifier clazz> {<ClassBodyDeclaration* dec>}`;
     }
   };
   return <total, unit>;
}