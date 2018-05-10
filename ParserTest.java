package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.AST.*;

import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class ParserTest {

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
	 * Simple test case with an empty program. This test expects an exception
	 * because all legal programs must have at least an identifier
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. Parsing should fail
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the tokens
		Parser parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}


	@Test
	public void testNameOnly() throws LexicalException, SyntaxException {
		String input = "prog";  //Legal program with only a name
		show(input);            //display input
		Scanner scanner = new Scanner(input).scan();   //Create scanner and create token list
		show(scanner);    //display the tokens
		Parser parser = new Parser(scanner);   //create parser
		Program ast = parser.parse();          //parse program and get AST
		show(ast);                             //Display the AST
		assertEquals(ast.name, "prog");        //Check the name field in the Program object
		assertTrue(ast.decsAndStatements.isEmpty());   //Check the decsAndStatements list in the Program object.  It should be empty.
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec.type.kind);
		assertEquals("k", dec.name);
		assertNull(dec.e);
	}
	
	@Test
	public void testDec2() throws LexicalException, SyntaxException {
		String input = "prog image ia;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Image dec = (Declaration_Image) ast.decsAndStatements
				.get(0);  
		//assertEquals(KW_int, dec.type.kind);
		assertEquals("ia", dec.name);
		//assertNull(dec.e);
	}
	
	@Test
	public void testDec3() throws LexicalException, SyntaxException {
		String input = "prog boolean xyz;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_boolean, dec.type.kind);
		assertEquals("xyz", dec.name);
		assertNull(dec.e);
	}
	
	@Test
	public void testDec4() throws LexicalException, SyntaxException {
		String input = "prog int k; int abc; boolean xyz; int ba = 5;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec1 = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec1.type.kind);
		assertEquals("k", dec1.name);
		assertNull(dec1.e);
		Declaration_Variable dec2 = (Declaration_Variable) ast.decsAndStatements
				.get(1);  
		assertEquals(KW_int, dec2.type.kind);
		assertEquals("abc", dec2.name);
		assertNull(dec2.e);
		Declaration_Variable dec3 = (Declaration_Variable) ast.decsAndStatements
				.get(2); 
		assertEquals(KW_boolean, dec3.type.kind);
		assertEquals("xyz", dec3.name);
		assertNull(dec3.e);
		Declaration_Variable dec4 = (Declaration_Variable) ast.decsAndStatements
				.get(3); 
		assertEquals(KW_int, dec4.type.kind);
		assertEquals("ba", dec4.name);
		Expression_IntLit ex= (Expression_IntLit) dec4.e;
		assertEquals(5, ex.value);
	}
	
	@Test
	public void testDec5() throws LexicalException, SyntaxException {
		String input = "prog url google = abdedf  ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_SourceSink dec = (Declaration_SourceSink) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_url, dec.type.KW_url);
		assertEquals("google", dec.name);
		Source_Ident s = (Source_Ident) dec.source;
		assertEquals("abdedf",s.name);
	}
	
	@Test
	public void testDec6() throws LexicalException, SyntaxException {
		String input = "myname is -> ankkita ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "myname"); 
		Statement_Out dec = (Statement_Out) ast.decsAndStatements.get(0);
		assertEquals("is", dec.name);
		Sink_Ident s = (Sink_Ident) dec.sink;
		assertEquals("ankkita",s.name);
	}
	
	@Test
	public void testDec7() throws LexicalException, SyntaxException {
		String input = "myname is <- \"ankkita\" ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "myname"); 
		Statement_In dec = (Statement_In) ast.decsAndStatements.get(0);
		assertEquals("is", dec.name);
		Source_StringLiteral s = (Source_StringLiteral) dec.source;
		assertEquals("ankkita",s.fileOrUrl);
	}
	
	@Test
	public void testDec8() throws LexicalException, SyntaxException {
		//String input = "prog image [xa, yb] abc <- @55;";
		//String input = "b+c+d+-e-+f+!g";
		String input = "b";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Expression ast = parser.expression();
		show(ast);
		//assertEquals(ast.name, "prog"); 
		/*Declaration_Image dec = (Declaration_Image) ast.decsAndStatements
				.get(0);
		Expression_PixelSelector xx = (Expression_PixelSelector)dec.xSize;
		Expression_PixelSelector yy = (Expression_PixelSelector)dec.ySize;
		assertEquals("xa", xx.name);
		assertEquals("yb", yy.name);
		assertEquals("abc", dec.name);
		Source_CommandLineParam s = (Source_CommandLineParam) dec.source;
		Expression_IntLit ex = (Expression_IntLit) s.paramNum;
		assertEquals(55,ex.value); */
	}
	/*
	@Test
	public void testDec9() throws LexicalException, SyntaxException {
		String input = "abc prog[[r,A]] = 55;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "abc"); 
		Statement_Assign dec = (Statement_Assign) ast.decsAndStatements.get(0);
		LHS lhs = dec.lhs;
		Expression_IntLit ex = (Expression_IntLit)dec.e;
		assertEquals("prog", lhs.name);
		Index 
		Expression_PixelSelector xx = (Expression_PixelSelector)dec.xSize;
		Expression_PixelSelector yy = (Expression_PixelSelector)dec.ySize;
		assertEquals("xa", xx.name);
		assertEquals("yb", yy.name);
		assertEquals("abc", dec.name);
		Source_CommandLineParam s = (Source_CommandLineParam) dec.source;
		Expression_IntLit ex = (Expression_IntLit) s.paramNum;
		assertEquals(55,ex.value);
	}

*/
	
	
	
}
