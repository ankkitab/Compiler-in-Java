package cop5556fa17;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.*;
import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;
	
	
	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	private void consume() {
		t= scanner.nextToken();
	}
	
	private void match(Kind kind) throws SyntaxException{
		if (t.kind==kind) 
			consume();
		else {
			throw new SyntaxException(t,"Error in matching the token "+t.kind);
		}
	}
	
	HashMap<String,HashSet<Kind>> PredictSets = new HashMap<>();
	
//Making PREDICT sets 
	public HashSet<Kind> makePredict(String type) {
		if (PredictSets.get(type)!=null) {
			return PredictSets.get(type);	
		}
		HashSet<Kind> PREDICT = new HashSet<>();
		
		switch (type) {
		case "Program" :
							PREDICT.add(IDENTIFIER);
							PredictSets.put(type, PREDICT);
							break;
		case "Declaration" : if (PredictSets.get("VariableDeclaration")==null)
									makePredict("VariableDeclaration");
							PREDICT.addAll(PredictSets.get("VariableDeclaration"));
							if (PredictSets.get("ImageDeclaration")==null)
								makePredict("ImageDeclaration");
							PREDICT.addAll(PredictSets.get("ImageDeclaration"));
							if (PredictSets.get("SourceSinkDeclaration")==null)
								makePredict("SourceSinkDeclaration");
							PREDICT.addAll(PredictSets.get("SourceSinkDeclaration"));
							PredictSets.put("Declaration", PREDICT);
							break;
		case "VariableDeclaration" : PREDICT.add(KW_int);
									PREDICT.add(KW_boolean);
									PredictSets.put("VariableDeclaration", PREDICT);
									break;
		case "SourceSinkDeclaration" :  PREDICT.add(KW_url);
										PREDICT.add(KW_file);
										PredictSets.put("SourceSinkDeclaration", PREDICT);
										break;
		case "ImageDeclaration" : PREDICT.add(KW_image);
								  PredictSets.put("ImageDeclaration", PREDICT);
								  break;
		case "Statement" : PREDICT.add(IDENTIFIER);
							PredictSets.put("Statement", PREDICT);
							  break;
		case "AssignmentStatement" : PREDICT.add(LSQUARE);
									PREDICT.add(OP_ASSIGN);
									PredictSets.put("AssignmentStatement", PREDICT);
										break;					
		case "ImageOutStatement" : PREDICT.add(OP_RARROW);
									PredictSets.put("ImageOutStatement", PREDICT);
										break;
		case "ImageInStatement" : PREDICT.add(OP_LARROW);
										PredictSets.put("ImageInStatement", PREDICT);
											break;
		case "UnaryExpressionNotPlusMinus" : PREDICT.add(OP_EXCL);
											if (PredictSets.get("Primary")==null)
												makePredict("Primary");
											PREDICT.addAll( PredictSets.get("Primary"));
											PREDICT.add(IDENTIFIER);
											PREDICT.add(KW_x);
											PREDICT.add(KW_y);
											PREDICT.add(KW_r);
											PREDICT.add(KW_a);
											PREDICT.add(KW_X);
											PREDICT.add(KW_Y);
											PREDICT.add(KW_Z);
											PREDICT.add(KW_A);
											PREDICT.add(KW_R);
											PREDICT.add(KW_DEF_X );
											PREDICT.add(KW_DEF_Y);
											PredictSets.put("UnaryExpressionNotPlusMinus", PREDICT);
											break;
		case "Primary" : PREDICT.add(INTEGER_LITERAL)	;
						PREDICT.add(LPAREN);
						if (PredictSets.get("FunctionApplication")==null)
							makePredict("FunctionApplication");
						PREDICT.addAll(PredictSets.get("FunctionApplication"));
						PREDICT.add(BOOLEAN_LITERAL);
						PredictSets.put("Primary", PREDICT);
						break;
		case "IdentOrPixelSelectorExpression" : PREDICT.add(IDENTIFIER);
												PredictSets.put("IdentOrPixelSelectorExpression", PREDICT);
												break;
		
		case "FunctionApplication" : 	PREDICT.add(KW_sin );
								PREDICT.add(KW_cos);
								PREDICT.add(KW_atan);
								PREDICT.add(KW_abs);
								PREDICT.add(KW_cart_x);
								PREDICT.add(KW_cart_y);
								PREDICT.add(KW_polar_a);
								PREDICT.add(KW_polar_r);
								
								PredictSets.put("FunctionApplication", PREDICT);
								break;
		case "XySelector" : PREDICT.add(KW_x);
							PredictSets.put("XySelector", PREDICT);
							break;
		case "RaSelector" : PREDICT.add(KW_r);
							PredictSets.put("RaSelector", PREDICT);
							break;
								
						
		}
		
		return PREDICT;
	}
	
	public HashSet<Kind> union(HashSet<Kind> orig, HashSet<Kind> curr) {
		for (Kind k:curr) {
			if (k!=null)
				orig.add(k);
		}
		return orig;
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO  implement this
		Program p1=null;
		Token t1= t;
		match(IDENTIFIER);
		p1 = programTail();
		if (p1!=null)
			return new Program(t1,t1,p1.decsAndStatements);
		else 
			return new Program(t1,t1, new ArrayList<>());
		//throw new SyntaxException(t,"Error in program");
	}
	
	Program programTail() throws SyntaxException {
		Program p0=null;
		ArrayList<ASTNode> decsandstate = new ArrayList<>();
		if (t.kind==EOF)
			return p0;
		if (makePredict("Declaration").contains(t.kind)) {
			Declaration d;
			d = declaration();
			decsandstate.add(d);
			match(SEMI);
			Program p;
			p=programTail();
			if (p!=null)
				decsandstate.addAll(p.decsAndStatements);
		}
		else if (makePredict("Statement").contains(t.kind)) {
			Statement s;
			s=statement();
			decsandstate.add(s);
			match(SEMI);
			Program p;
			p=programTail();
			if (p!=null)
				decsandstate.addAll(p.decsAndStatements);
		}
		else
			throw new SyntaxException(t,"Error in programTail");
		p0= new Program(decsandstate.get(0).firstToken,decsandstate.get(0).firstToken,decsandstate);
		return p0;
	}
	
	Declaration declaration() throws SyntaxException {
		if (makePredict("VariableDeclaration").contains(t.kind)) 
			 return variableDeclaration();
		else if (makePredict("ImageDeclaration").contains(t.kind)) 
			return imageDeclaration();
		else if (makePredict("SourceSinkDeclaration").contains(t.kind)) 
			return sourceSinkDeclaration();
		else 
			throw new SyntaxException(t,"Error in declaration");
	}
	
	Declaration variableDeclaration() throws SyntaxException {
			Token type = t;
			varType();
			Token id = t;
			Expression e=null;
			match (IDENTIFIER);
			if (t.kind==OP_ASSIGN) {
				match(OP_ASSIGN);
				e= expression();
			}
			Declaration d = new Declaration_Variable(type,type,id,e);
			return d;
	}
	
	void varType() throws SyntaxException {
		if (t.kind==KW_int)
			match(KW_int);
		else if (t.kind==KW_boolean)
			match(KW_boolean);
		else
			throw new SyntaxException(t,"Error in varType");
	}
	
	Declaration sourceSinkDeclaration() throws SyntaxException {
		Token first = t;
		sourceSinkType();
		Token id = t;
		match(IDENTIFIER);
		match(OP_ASSIGN);
		Source s;
		s=source(); 
		Declaration d = new Declaration_SourceSink(first,first,id,s);
		return d;
	}
	
	Source source()  throws SyntaxException{
		Token t0=null;
		Source s=null;
		if (t.kind==STRING_LITERAL) {
			t0=t;
			match(STRING_LITERAL);
			s= new Source_StringLiteral(t0,t0.getText());
		}
		else if (t.kind==OP_AT) {
			match(OP_AT);
			Expression e;
			e = expression();
			s= new Source_CommandLineParam(e.firstToken,e);
		}
		else if (t.kind==IDENTIFIER) {
			t0=t;
			match(IDENTIFIER);
			s= new Source_Ident(t0, t0);
		}
		else
			throw new SyntaxException(t,"Error in source");
		return s;
	}
	
	void sourceSinkType() throws SyntaxException {
		if (t.kind==KW_url)
			match(KW_url);
		else if (t.kind==KW_file)
			match(KW_file);
		else
			throw new SyntaxException(t,"Error in sourceSinkType");
	}
	
	Declaration imageDeclaration() throws SyntaxException {
		Token first = t;
		match(KW_image);
		Expression e0=null;
		Expression e1=null;
		if (t.kind==LSQUARE) {
			match(LSQUARE);
			e0=expression();
			match(COMMA);
			e1=expression();
			match(RSQUARE);
		}
		Token id=t;
		match(IDENTIFIER);
		Source s = null;
		if (t.kind==OP_LARROW) {
			match(OP_LARROW);
			s=source();
		}
		if (e0!=null)
			return new Declaration_Image(e0.firstToken,e0,e1,id,s);
		else
			return new Declaration_Image(id,e0,e1,id,s);
	}
	
	Statement statement() throws SyntaxException {
		Token id=t;
		match(IDENTIFIER);
		Statement s;
		s= statementTail(id);
		return s;
	}
	
	Statement statementTail(Token id) throws SyntaxException {
		Statement s;
		if (makePredict("AssignmentStatement").contains(t.kind)) 
			return assignmentStatement(id);
		else if (makePredict("ImageOutStatement").contains(t.kind)) 
			return imageOutStatement(id);
		else if (makePredict("ImageInStatement").contains(t.kind)) 
			return imageInStatement(id);
		else 
			throw new SyntaxException(t,"Error in statementTail");
	}
	
	Statement assignmentStatement(Token id) throws SyntaxException {
		LHS l;
		l=lhsNew(id);
		match(OP_ASSIGN);
		Expression e;
		e=expression();
		return new Statement_Assign(l.firstToken,l,e);
	}
	
	LHS lhsNew(Token id) throws SyntaxException {
		Index i=null;
		if (t.kind==LSQUARE) {
			match(LSQUARE);
			i=lhsSelector();
			match(RSQUARE); 
		}
		return new LHS(id,id,i);
	}
	Statement imageOutStatement(Token id) throws SyntaxException {
		match(OP_RARROW);
		Sink s;
		s=sink();
		return new Statement_Out(id,id,s);
	}
	Statement imageInStatement(Token id) throws SyntaxException {
		match(OP_LARROW);
		Source s;
		s=source();
		return new Statement_In(id,id,s);
	}
	
	Sink sink() throws SyntaxException {
		Sink s;
		Token t0 = t;
		if (t.kind==IDENTIFIER) {
			match(IDENTIFIER);
			s= new Sink_Ident(t0,t0);
			return s;
		}
		else if (t.kind==KW_SCREEN) {
			match(KW_SCREEN);
			s= new Sink_SCREEN(t0);
			return s;
		}
		else
			throw new SyntaxException(t,"Error in sink");
	}
	
	
	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		//TODO implement this.
		Expression e0;
		e0=orExpression();
		if (t.kind==OP_Q)
			e0=expressionTail(e0);
		return e0;
	}
	Expression expressionTail(Expression e0) throws SyntaxException {
		Expression e1=null;
		Expression e2=null;
		if (t.kind==OP_Q) {
			match(OP_Q);
			e1 = expression();
			match(OP_COLON);
			e2 = expression();
		}
		return new Expression_Conditional(e0.firstToken,e0,e1,e2);
	}
	
	Expression orExpression() throws SyntaxException {
		Expression e0=null;
		e0=andExpression();
		Token t0=null;
		while (t.kind==OP_OR) {
			t0=t;
			match(OP_OR);
			e0= new Expression_Binary(e0.firstToken,e0,t0,andExpression());
		}
		return e0;
	}
	Expression andExpression() throws SyntaxException {
		Expression e0 = eqExpression();
		Token t0=null;
		while (t.kind==OP_AND) {
			t0=t;
			match(OP_AND);
			e0= new Expression_Binary(e0.firstToken, e0, t0, eqExpression());
		}
		return e0;
	}
	Expression eqExpression() throws SyntaxException {
		Expression e0 = relExpression();
		Token t0 = null;
		while (t.kind==OP_EQ || t.kind==OP_NEQ) {
			if (t.kind==OP_EQ) {
				t0=t;
				match(OP_EQ);
			}
			else if (t.kind==OP_NEQ){
				t0=t;
				match(OP_NEQ);
			}
			e0= new Expression_Binary(e0.firstToken, e0, t0, relExpression());
		}
		return e0;
	}
	Expression relExpression() throws SyntaxException {
		Expression e0=addExpression();
		Token t0 = null;
		while (t.kind==OP_LT || t.kind==OP_GT || t.kind==OP_LE || t.kind==OP_GE) {
			if (t.kind==OP_LT) {
				t0=t;
				match(OP_LT);
			}
			else if (t.kind==OP_GT) {
				t0=t;
				match(OP_GT);
			}
			else if (t.kind==OP_LE){
				t0=t;
				match(OP_LE);
			}
			else if (t.kind==OP_GE){
				t0=t;
				match(OP_GE);
			}
			e0 = new Expression_Binary(e0.firstToken, e0, t0, addExpression());
		}
		return e0;
	}
	
	Expression addExpression() throws SyntaxException {
		Expression e0=multiExpression();
		Token t0 = null;
		while (t.kind==OP_PLUS  || t.kind==OP_MINUS) {
			if (t.kind==OP_PLUS) {
				t0=t;
				match(OP_PLUS);
			}
			else if (t.kind==OP_MINUS) {
				t0=t;
				match(OP_MINUS);
			}
			e0 = new Expression_Binary(e0.firstToken, e0, t0, multiExpression());
		}
		return e0;
	}
	
	Expression multiExpression() throws SyntaxException {
		Expression e0=unaryExpression();
		Token t0 = null;
		while (t.kind==OP_TIMES  || t.kind==OP_DIV || t.kind==OP_MOD) {
			if (t.kind==OP_TIMES) {
				t0=t;
				match(OP_TIMES);
			}
			else if (t.kind==OP_DIV) {
				t0=t;
				match(OP_DIV);
			}
			else if (t.kind==OP_MOD) {
				t0=t;
				match(OP_MOD);
			}
			e0 = new Expression_Binary(e0.firstToken, e0, t0, unaryExpression());
		}
		return e0;
	}
	
	Expression unaryExpression() throws SyntaxException {
		Token t0=null; Expression e0=null;
		if (t.kind==OP_PLUS) {
			t0=t;
			match(OP_PLUS);
			e0= new Expression_Unary(t0, t0, unaryExpression());
		}
		else if (t.kind==OP_MINUS) {
			t0=t;
			match(OP_MINUS);
			e0= new Expression_Unary(t0, t0, unaryExpression());
		}
		else if(makePredict("UnaryExpressionNotPlusMinus").contains(t.kind)) {
			e0=unaryExpressionNotPlusMinus();
		}
		else
			throw new SyntaxException(t,"Error in unaryExpression()");
		return e0;
	}
	
	Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Token t0=null; Expression e0=null;
		if (t.kind==OP_EXCL) {
			t0=t;
			match(OP_EXCL);
			e0= new Expression_Unary(t0, t0, unaryExpression());
		}
		else if(makePredict("Primary").contains(t.kind)) 
			e0=primary();
		else if(makePredict("IdentOrPixelSelectorExpression").contains(t.kind)) 
			e0=identOrPixelSelectorExpression();
		else {
			t0=t;
			switch(t.kind) {
			case KW_x: match(KW_x); break;
			case KW_y: match(KW_y); break;
			case KW_r: match(KW_r); break;
			case KW_a: match(KW_a); break;
			case KW_X: match(KW_X); break;
			case KW_Y: match(KW_Y); break;
			case KW_Z: match(KW_Z); break;
			case KW_A: match(KW_A); break;
			case KW_R: match(KW_R); break;
			case KW_DEF_X : match(KW_DEF_X ); break;
			case KW_DEF_Y : match(KW_DEF_Y ); break;
			default : throw new SyntaxException(t,"Error in unaryExpressionNotPlusMinus");
			}
			e0 = new Expression_PredefinedName(t0,t0.kind);
		}
		return e0;
	}
	
	Expression primary() throws SyntaxException {
		Expression e0=null;
		Token t0=t;
		if (t.kind==INTEGER_LITERAL) {
			match(INTEGER_LITERAL);
			e0= new Expression_IntLit(t0, t0.intVal());
		}
		else if (t.kind==LPAREN) {
			match(LPAREN);
			e0=expression();
			match(RPAREN);
		}
		else if (t.kind==BOOLEAN_LITERAL) {
			match(BOOLEAN_LITERAL);
			e0 = new Expression_BooleanLit(t0, Boolean.parseBoolean(t0.getText()));
		}
		else if(makePredict("FunctionApplication").contains(t.kind)) 
			e0= functionApplication();
		else 
			throw new SyntaxException(t,"Error in primary");
		return e0;
	}
	
	Expression identOrPixelSelectorExpression() throws SyntaxException{
		Token t0=t;
		match(IDENTIFIER);
		Expression e0= identOrPixelSelectorExpressionTail(t0);
		return e0;
	}
	
	Expression identOrPixelSelectorExpressionTail(Token t0) throws SyntaxException{
		Index i = null;
		if (t.kind==LSQUARE) {
			match(LSQUARE);
			i=selector();
			match(RSQUARE);
			return new Expression_PixelSelector(t0,t0,i);
		}
		else 
			return new Expression_Ident(t0,t0);
		
	}
	void lhs() throws SyntaxException{
		match(IDENTIFIER);
		if (t.kind==LSQUARE) {
			match(LSQUARE);
			lhsSelector();
			match(RSQUARE);
		}
	}
	
	Expression functionApplication() throws SyntaxException {
		Token t0= t;
		functionName();
		return functionApplicationTail(t0);
	}
	
	Expression functionApplicationTail(Token t0) throws SyntaxException {
		Expression e0=null;
		if (t.kind==LPAREN) {
			match(LPAREN);
			e0= new Expression_FunctionAppWithExprArg(t0, t0.kind,expression());		
			match(RPAREN);
		}
		else if (t.kind==LSQUARE) {
			match(LSQUARE);
			e0= new Expression_FunctionAppWithIndexArg(t0, t0.kind, selector());
			match(RSQUARE);
		}
		else throw new SyntaxException(t,"Error in functionApplicationTail");
		return e0;	
	}
	
	void functionName() throws SyntaxException {
		switch(t.kind) {
		case KW_sin: match(KW_sin); break;
		case KW_cos: match(KW_cos); break;
		case KW_atan: match(KW_atan); break;
		case KW_abs: match(KW_abs); break;
		case KW_cart_x: match(KW_cart_x); break;
		case KW_cart_y: match(KW_cart_y); break;
		case KW_polar_a : match(KW_polar_a); break;
		case KW_polar_r: match(KW_polar_r); break;
		default : throw new SyntaxException(t,"Error in functionName");
		}
			
	}
	
	Index lhsSelector() throws SyntaxException {
		Index lhs;
		if (t.kind==LSQUARE) {
			match(LSQUARE);
			lhs=lhsSelectorTail();
			return lhs;
		}
		else
			throw new SyntaxException(t, "Error in LHS Selector");
	}
	Index lhsSelectorTail() throws SyntaxException {
		Index exprPre=null;
		if (makePredict("XySelector").contains(t.kind)) {
			exprPre=xySelector();
			match(RSQUARE);
			return exprPre;
		}
		else if (makePredict("RaSelector").contains(t.kind)){
			exprPre=raSelector();
			match(RSQUARE);
			return exprPre;
		}
		else
			throw new SyntaxException(t, "Error in LHSSelectorTail");
		
	}
	
	Index xySelector() throws SyntaxException {
		Token t0=t;
		Expression e0=null;
		Expression e1=null;
		match(KW_x);
		e0= new Expression_PredefinedName(t0,KW_x);
		match(COMMA);
		Token t1=t;
		match(KW_y);
		e1= new Expression_PredefinedName(t1,KW_y);
		
		return new Index(t0,e0,e1);
	}
	
	Index raSelector() throws SyntaxException {
		Token t0=t;
		Expression e0=null;
		Expression e1=null;
		match(KW_r);
		e0= new Expression_PredefinedName(t0,KW_r);
		match(COMMA);
		Token t1=t;
		match(KW_a);
		e1= new Expression_PredefinedName(t1,KW_a);
		
		return new Index(t0,e0,e1);
	}
	
	Index selector() throws SyntaxException {
		Expression e0= expression();
		match(COMMA);
		Expression e1= expression();
		return new Index(e0.firstToken,e0,e1);
	}
	

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
