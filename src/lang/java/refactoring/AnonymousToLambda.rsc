module lang::java::refactoring::AnonymousToLambda

import lang::java::\syntax::Java18;
import lang::java::analysis::Imports;

import ParseTree; 
import IO;

void findAnonymousInnerClass(CompilationUnit unit) {
   visit(unit) {
     case (Expression)`new <Identifier id>() <ClassBody body>` : { 
         println("AIC new <id> <body>"); 
      } 
   };
}


public tuple[int, CompilationUnit] refactorAnonymousInnerClass(CompilationUnit unit) {
   list[ImportClause] imports = listOfImports(unit);
   int total = 0;
   list[int] fails = [0, 0, 0, 0, 0, 0];
   CompilationUnit res = visit(unit) {
     case (Expression)`new <ClassOrInterfaceTypeToInstantiate id>() {<MethodModifier m> <Result res> <Identifier methodName> () { <BlockStatements stmt> } }` : 
     { check = checkConstraints(stmt, methodName, imports); 
       if(check == 0) {
       println("aoo"); 
         total += 1;
         insert (Expression)`()-\> { <Statement stmt >}`;
       }
       else {
       	println("erro: <check>");
          fails[check] = fails[check] + 1;
       }
     }
     case (Expression)`new <ClassOrInterfaceTypeToInstantiate id>() {<MethodModifier m> <Result res> <Identifier methodName> (<FormalParameter fp>) {<BlockStatements stmt>}}` : 
     {  check = checkConstraints(stmt, methodName, imports); 
     	if(check == 0) {
     		println("aoo");
     	  total += 1;
          insert (Expression)`(<FormalParameter fp>)-\>{ <Statement stmt>}`;
        }
        else {
        	println("erro: <check>");
           fails[check] = fails[check] + 1;
        }   
     }
   };
   //if some check failed to the compilation unit, we can calculate the fail. 
   //in this way, we are able to estimate the occurence of constraints that 
   //often fail.
   if(!(true | it && (v == 0) | int v <- fails)) { 
     println(fails);
   } 
   return <total, res>;
}

/**
 * Check the constraints related to the 
 * annonymousToLambda refactoring. 
 */
 
 
int checkConstraints(BlockStatements stmt, Identifier methodName, list[ImportClause] imports)  {
  res = 0; 
  println(methodName);
  visit(stmt) { 
    case (Expression)`this` : {println("calls this"); res = 1;}
    case (FieldAccess)`super.<Identifier id>` : {println("access super field"); res = 2;}
    case (MethodInvocation)`super.<TypeArguments args><Identifier id>(<ArgumentList args>)` : {println("invokes super method");res = 3;}
    case (MethodInvocation)`methodName(<ArgumentList args>)` : {println("recursive call");res = 4;}
    case (MethodInvocation)`methodName()` : {println("recursive call");res = 4;}  
    case (ThrowStatement)`throw new <ClassOrInterfaceTypeToInstantiate e>();` : 
    { 
    	if(isCheckedException(unparse(e))) {
    		println("throws checked exception in  annonymous " + e); res = 5;
    	}
    }
  };
  return res; 
}

 @javaClass{com.rascaljava.RascalJavaInterface}
java bool isCheckedException(str exc);