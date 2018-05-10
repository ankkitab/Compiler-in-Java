package cop5556fa17;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;

import static cop5556fa17.Scanner.Kind.*;

public class TypeCheckTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	
	/**
	 * Scans, parses, and type checks given input String.
	 * 
	 * Catches, prints, and then rethrows any exceptions that occur.
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		try {
			Scanner scanner = new Scanner(input).scan();
			ASTNode ast = new Parser(scanner).parse();
			show(ast);
			ASTVisitor v = new TypeCheckVisitor();
			ast.visit(v, null);
		} catch (Exception e) {
			show(e);
			throw e;
		}
	}

	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSmallest() throws Exception {
		String input = "n"; //Smallest legal program, only has a name
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); // Create a parser
		ASTNode ast = parser.parse(); // Parse the program
		TypeCheckVisitor v = new TypeCheckVisitor();
		String name = (String) ast.visit(v, null);
		show("AST for program " + name);
		show(ast);
	}



	
	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	 @Test
	 public void testDec1() throws Exception {
	 String input = "prog int k = 42;";
	 typeCheck(input);
	 }
	 
	 /**
	  * This program does not declare k. The TypeCheckVisitor should
	  * throw a SemanticException in a fully implemented assignment.
	  * @throws Exception
	  */
	 @Test
	 public void testUndec() throws Exception {
	 String input = "prog k = 42;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test2() throws Exception {
	 String input = "prog url google = abdedf  ;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test3() throws Exception {
	 String input = "prog int k =5 ; boolean xyz = true ; int ba = 5;";
	// thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test4() throws Exception {
	 String input = "myname is <- \"ankkita\" ;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test5sourceSink() throws Exception {
	 String input = "myname file is = \"abc\"; is <- \"ankkita\" ;";
	// thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test6index() throws Exception {
	 String input = "prog int abc; abc[[x,y]]=45; abc=abc*8;";
	//thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test7mulExp() throws Exception {
	 String input = "prog int k=3; k=k*5; k=k+5;";
	//thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test8url() throws Exception {
	 String input = "prog url ab = \"https://www.google.com\"; ";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test9url() throws Exception {
	 String input = "prog url ab = \"https://www.google.com\"; ab <- \"https://www.foogle.com\" ;";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test10screen() throws Exception {
	 String input = "prog int ab = 5; ab -> SCREEN ;";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test11ind() throws Exception {
	 String input = "prog int ab = 5; int k; int j; ab[[x,y]] = k+4*j ;";
	// thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void test12exp() throws Exception {
	 String input = "prog int ab = 5; int k; boolean boo; ab = k*k-k; boo= k<ab;";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	
	 
	 @Test
	 public void wrong1() throws Exception {
	 String input = "prog int k=k+1;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	
	 
	 @Test
	 public void wrong2() throws Exception {
	 String input = "p int n; n = sin(30)/cos(40);\n";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	
	 
	 @Test
	 public void wrong3() throws Exception {
	 String input = "prog boolean bool = true; boolean bool2; bool -> bool2;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	
	 
	 @Test
	 public void wrong4() throws Exception {
	 String input = "prog file s = \"some source\"; image [4,5] i <- s;";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	
	 
	 @Test
	 public void wrong5() throws Exception {
	 String input = "prog file s = \"some source\"; image i; i -> s;";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	
	 
	 

}
	