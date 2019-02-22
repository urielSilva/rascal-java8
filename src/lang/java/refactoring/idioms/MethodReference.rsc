module lang::java::refactoring::idioms::MethodReference

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
       	  listTypes = addMethodParams(methodHeader, listTypes);
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
     			listNameStr = trim(unparse(listName));
      	  		refactoredArgs = visit(methodArgs) {
      	  			case(Expression)`(<Identifier id>) -\> <ExpressionName name>.<TypeArguments? typeArgs><Identifier methodName>(<ArgumentList? args>)` : {
  	  					<parsed, methodReference> = parseMethodReference(trim(unparse(id)), trim(unparse(name)), trim(unparse(methodName)), listNameStr, listTypes);
  	  					if(parsed) {
  	  						total += 1;
  	  						insert methodReference;
  	  					}
		     		}
		     		
		     		case(Expression)`<Identifier id> -\> <ExpressionName name>.<TypeArguments? typeArgs><Identifier methodName>(<ArgumentList? args>)` : {
      	  				if(trim(unparse(listName)) in listTypes) {
      	  					<parsed, methodReference> = parseMethodReference(trim(unparse(id)), trim(unparse(name)), trim(unparse(methodName)), listNameStr,listTypes);
	  	  					if(parsed) {
	  	  						total += 1;
	  	  						insert methodReference;
	  	  					}
      	  				}
     				}
      	  		}
      	  		if(refactoredArgs != methodArgs) {
      	  			insert (MethodInvocation) `<ExpressionName listName>.<Identifier methodName>(<ArgumentList refactoredArgs>)`;
      	  		}
      	  		
     		}
     		
     		case(MethodInvocation)`<Primary primary>.<Identifier methodName>(<ArgumentList? methodArgs>)` : {
     			listName = trim(head(split(".", unparse(primary))));
      	  		refactoredArgs = visit(methodArgs) {
      	  			case(Expression)`(<Identifier id>) -\> <ExpressionName name>.<TypeArguments? typeArgs><Identifier methodName>(<ArgumentList? args>)` : {
      	  				if(listName in listTypes) {
      	  					<parsed, methodReference> = parseMethodReference(trim(unparse(id)), trim(unparse(name)), trim(unparse(methodName)), listName, listTypes);
	  	  					if(parsed) {
	  	  						total += 1;
	  	  						insert methodReference;
	  	  					}
      	  				}
		     		}
		     		
		     		case(Expression)`<Identifier id> -\> <ExpressionName name>.<TypeArguments? typeArgs><Identifier methodName>(<ArgumentList? args>)` : {
      	  				if(listName in listTypes) {
      	  					<parsed, methodReference> = parseMethodReference(trim(unparse(id)), trim(unparse(name)), trim(unparse(methodName)), listName, listTypes);
	  	  					if(parsed) {
	  	  						total += 1;
	  	  						insert methodReference;
	  	  					}
      	  				}
     				}
      	  		}
      	  		if(refactoredArgs != methodArgs) {
      	  			insert (MethodInvocation) `<ExpressionName listName>.<Identifier methodName>(<ArgumentList refactoredArgs>)`;
      	  		}
     		}
  	  	}
  	  	 if(refactoredBody != mBody) {
  		   insert(MethodDeclaration) `<MethodModifier* mds> <MethodHeader methodHeader> <MethodBody refactoredBody>`;
  		 }
      	 
     	}
   };
   return <total, refactoredUnit>;
}


public tuple[bool, Expression] parseMethodReference(str lambdaArgId, lambdaVarId, str methodName, str listName, map[str, str] listTypes) {
	if(listName in listTypes && lambdaArgId == lambdaVarId) {
		methodRef = parse(#MethodReference, "<listTypes[listName]>::<methodName>");
  		return <true, parse(#Expression, unparse(methodRef))>;
	} else {
		methodRef = parse(#MethodReference, "false::<methodName>"); // this will never be used
		return <false, parse(#Expression, unparse(methodRef))>;
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