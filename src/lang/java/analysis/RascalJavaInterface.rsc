module lang::java::analysis::RascalJavaInterface


import String;


@javaClass{com.rascaljava.RascalJavaInterface}
java int initDB(str projectPath);

@javaClass{com.rascaljava.RascalJavaInterface}
java bool isRelated(str clazzA, str clazzB);

@javaClass{com.rascaljava.RascalJavaInterface}
java bool isCollection(str className, str fieldName);