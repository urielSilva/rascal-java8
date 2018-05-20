module lang::java::refactoring::libs::JUnitTestCaseDeclaration

import ParseTree;
import lang::java::\syntax::Java18;
import IO;


/**
 * Refactor a compilation unit to replace 
 * a JUnit 3 test case declaration for a JUnit 4 one
 */ 
public tuple[int, CompilationUnit] refactorJUnitTestCaseDeclaration(CompilationUnit cu) {
	int total = 0;
	println(cu); 
   
   	CompilationUnit unit =  visit(cu) {
    		case(MethodDeclaration)`public void <Identifier name>() <MethodBody body>`: {
			if(/test<rest:[A-Za-z0-9]*>/ := "<name>") {
				total += 1;
				newName = [Identifier] rest;
				insert (MethodDeclaration)`@Test public void <Identifier newName>() <MethodBody body>`;
			};
		}
	};
	
	if(total > 0) {
      unit = visit(unit) {
         case(Imports)`<ImportDeclaration* imports>` : {
           insert (Imports)`<ImportDeclaration* imports>import org.junit.Test;`;
         } 
      }
   }
   return <total, unit>;
}