module lang::java::refactoring::IntegerSumPattern

import ParseTree;
import lang::java::\syntax::Java18;
import IO;


/**
 * Refactor a compilation unit to replace 
 * foreach statements which sum the values
 * of an integer iterable, into a lambda expression.
 */ 
public tuple[int, CompilationUnit] refactorIntegerSumPattern(CompilationUnit cu) {
   int total = 0;
   println(cu); 
   CompilationUnit unit =  visit(cu) {
       case(BlockStatement)`for(<UnannType t> <Identifier var> : <Expression exp>) {<Identifier sum> += <Identifier var>;}`: {
      	  total += 1;
          insert (BlockStatement)`<Identifier sum> = <Identifier exp>.stream().mapToInt(Integer::intValue).sum();`;
     }
   };
   return <total, unit>;
}