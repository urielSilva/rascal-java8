module lang::java::refactoring::forloop::EnhancedLoopExpression

import lang::java::\syntax::Java18;
import ParseTree;
import String;
import IO;
import lang::java::refactoring::forloop::MethodVar;
import lang::java::analysis::RascalJavaInterface;
import lang::java::analysis::Imports;

// XXX Only checking iterable variables defined in method (local and parameter)
// Need to verify class and instance variables too!
// Doing the full check on a method call will be an entire new problem
// example: for (Object rowKey : table.rowKeySet())

// Relying on compiler to help finding if it's an array or not
// Compiler gives error if expression is not Array/Collection
// Therefore we only check if the expression is an Array
public bool isIteratingOnCollection(CompilationUnit unit, Expression exp, set[MethodVar] availableVariables) {
	println(availableVariables);
	if (!isMethodInvocation(exp))
		return isIdentifierACollection(unit, exp, availableVariables);
	else
		return false;
}

// XXX Ignoring Casts too.
// Redundant for now, because any method invocation will contain '('
// But not everything that have '(' will be a method invocation. (Casts for instance)
private bool isMethodInvocation(Expression exp) {
	expStr = "<exp>";
	return contains(expStr, "(") && parsesAsMethodInvocation(expStr);
}

private bool parsesAsMethodInvocation(str expStr) {
	try {
		parse(#MethodInvocation, expStr);
		return true;
	} catch:
		return false;
}

private bool isIdentifierACollection(CompilationUnit unit, Expression exp, set[MethodVar] availableVariables) {
	varName = trim(unparse(exp));
	// TODO eventually change/remove when dealing correctly with fields + local variables 
	varName = replaceFirst(varName, "this.", "");
	className = findCurrentClassName(unit);
	var = findByName(availableVariables, varName);
	if(var.isClassField) {
		return isCollection(className, var.name);
	} else {
		return !isTypePlainArray(var) && !isIterable(var);
	}
}

// FIXME
private bool isExpressionReturningACollection(Expression exp) {
	return false;
}

public str findCurrentClassName(CompilationUnit unit) {
	top-down-break visit(unit) {
		case(NormalClassDeclaration) `<ClassModifier* _> class <Identifier className> <TypeParameters? _> <Superclass? super> <Superinterfaces? inter> <ClassBody _>`: {
			package = getPackage(unit);
			return "<package>.<className>";
		}
	}
	return "";
}
