module lang::java::refactoring::evolution::MethodReference

import ParseTree;
import lang::java::\syntax::Java18;
import IO;
import Node;
import String;

/**
 * Refactor a compilation unit to replace 
 * foreach statements, according to the 
 * exist pattern, into a lambda expression.
 */ 
public tuple[int, CompilationUnit] refactorMethodReference(CompilationUnit cu) {
   int total = 0; 
   map[str, str] listTypes = ();
   CompilationUnit refactoredUnit =  visit(cu) {
       case(MethodDeclaration)`<MethodModifier* mds> <MethodHeader methodHeader> <MethodBody mBody>` : {
      	  refactoredBody = visit(mBody) {
      	  	case(LocalVariableDeclaration) `<VariableModifier* modif> <UnannType varType> <VariableDeclaratorId varName> = <VariableInitializer init>`: {
      	  		if(/\<<lType:.*>\>/ := unparse(varType)) {
      	  			listTypes[trim(unparse(varName))] = trim(lType);
      	  		}
     		}
     		
     		case(LocalVariableDeclaration) `<VariableModifier* modif> <UnannType varType> <VariableDeclaratorId varName>`: {
      	  		if(/\<<lType:.*>\>/ := unparse(varType)) {
      	  			listTypes[trim(unparse(varName))] = trim(lType);
      	  		}
     		}
     		case(MethodInvocation)`<ExpressionName listName>.<Identifier methodName>(<ArgumentList? methodArgs>)` : {
      	  		refactoredArgs = visit(methodArgs) {
      	  			case(Expression)`(<Identifier id>) -\> <ExpressionName name>.<TypeArguments? typeArgs><Identifier methodName>(<ArgumentList? args>)` : {
      	  				if(trim(unparse(listName)) in listTypes) {
      	  					insertMethodReference(trim(unparse(id)), trim(unparse(name)), trim(unparse(methodName)), listTypes[trim(unparse(listName))]);
      	  				} else {
      	  					listTypes = addMethodParams(methodHeader, listTypes);
      	  					println(listTypes);
      	  					insertMethodReference(trim(unparse(id)), trim(unparse(name)), trim(unparse(methodName)), listTypes[trim(unparse(listName))]);
      	  				}
      	  				
		     		}
		     		
		     		case(Expression)`<Identifier id> -\> <ExpressionName name>.<TypeArguments? typeArgs><Identifier methodName>(<ArgumentList? args>)` : {
      	  				if(trim(unparse(listName)) in listTypes) {
      	  					insertMethodReference(trim(unparse(id)), trim(unparse(name)), trim(unparse(methodName)), listTypes[trim(unparse(listName))]);
      	  				}
     				}
      	  		}
      	  		insert (MethodInvocation) `<ExpressionName listName>.<Identifier methodName>(<ArgumentList refactoredArgs>)`;
     		}
  	  	}
      	 insert(MethodDeclaration) `<MethodModifier* mds> <MethodHeader methodHeader> <MethodBody refactoredBody>`;
     	}
   };
   
   return <0, refactoredUnit>;
}


public void insertMethodReference(str lambdaArgId, lambdaVarId, str methodName, str listType) {
	if(lambdaArgId == lambdaVarId) {
  		methodRef = parse(#MethodReference, "<listType>::<methodName>");
  		insert parse(#Expression, unparse(methodRef));	
	}
} 

public map[str, str] addMethodParams(MethodHeader methodHeader, map[str, str] listTypes) {
	visit(methodHeader) {
		case(MethodDeclarator) `<Identifier methodName>(<UnannType argType> <Identifier argName>)`: {
			if(/\<<lType:.*>\>/ := unparse(argType)) {
				listTypes[trim(unparse(argName))] = trim(lType);
			}	
		}
	}
	return listTypes;
}