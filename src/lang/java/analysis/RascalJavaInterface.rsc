module lang::java::analysis::RascalJavaInterface


import String;


@javaClass{com.rascaljava.RascalJavaInterface}
java int initDB(str projectPath, str sourcePath);

@javaClass{com.rascaljava.RascalJavaInterface}
java bool isRelated(str clazzA, str clazzB);