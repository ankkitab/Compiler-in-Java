# Compiler-in-Java

About the project 
---------------------------
This is a compiler for a small programming language written in Java. Therefore this has a Lexical Analyser, a parser which performs Syntax and Semantic Analysis by constructing an Abstract Syntax Tree. It also performs type checking on the AST generated. The target language is java byte code and we used ASM byte code framework to help with code generation.


How to test it 
---------------------------
Since this project was done incrementally, each of the parts can be tested separately. 
I have added test files for all starting with ScannerTest.java, ParserTest.java to CodeGenVisitorTest.java

The project can be imported under package name cop5566fa17
Compile the project from command line using the following  - 
javac -cp .:/usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar cop5556fa17/*.java

Run junit test - 
java -cp .:/usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar org.junit.runner.JUnitCore cop5556fa17.CodeGenVisitorTest

