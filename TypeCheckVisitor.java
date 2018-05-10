package cop5556fa17;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		//Symbol Table implementation
		public class SymbolTable {
			HashMap<String, Object> sTable;
			
			public SymbolTable() {
				sTable = new HashMap<>();
			}
			public Object lookupType(String name) {
				return sTable.get(name);
			}
			
			public void insert (String name, Object obj) {
				sTable.put(name, obj);
			}
		}
		
		SymbolTable symbolTable = new SymbolTable();
	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	//done
	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = declaration_Variable.name;
		if (symbolTable.lookupType(name)==null) {
			if (declaration_Variable.e!=null) {
				Expression e = (Expression) declaration_Variable.e.visit(this, arg);
				declaration_Variable.setType(TypeUtils.getType(declaration_Variable.firstToken));
				symbolTable.insert(name, declaration_Variable);
				if (declaration_Variable.TYPE !=e.TYPE)
					throw new SemanticException(declaration_Variable.firstToken, "Type mismatch in Dec_Variable");
			}
			else {
				declaration_Variable.setType(TypeUtils.getType(declaration_Variable.firstToken));
				symbolTable.insert(name, declaration_Variable);
			}
		}
		else
			throw new SemanticException(declaration_Variable.firstToken, "First token is not null in Dec_Variable");
		return declaration_Variable;
	}

	//done
	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = (Expression) expression_Binary.e0.visit(this, arg);
		Expression e1 = (Expression) expression_Binary.e1.visit(this, arg);
		
		if (e0.TYPE==e1.TYPE) {
			Kind op = (Kind) expression_Binary.op;
			if (op==Kind.OP_EQ || op==Kind.OP_NEQ)
				expression_Binary.TYPE= Type.BOOLEAN;
			else if ((op==Kind.OP_GE || op==Kind.OP_GT || op==Kind.OP_LE || op==Kind.OP_LT) && (e0.TYPE==Type.INTEGER))
					expression_Binary.TYPE = Type.BOOLEAN;
			else if ((op==Kind.OP_AND || op==Kind.OP_OR) && (e0.TYPE==Type.INTEGER || e0.TYPE==Type.BOOLEAN))
				expression_Binary.TYPE = e0.TYPE;
			else if ((op==Kind.OP_DIV || op==Kind.OP_MINUS || op==Kind.OP_MOD || op==Kind.OP_PLUS || op==Kind.OP_POWER || op == Kind.OP_TIMES) && (e0.TYPE==Type.INTEGER))
				expression_Binary.TYPE=Type.INTEGER;
			else 
				expression_Binary.TYPE=Type.NONE;
			if (expression_Binary.TYPE==Type.NONE)
				throw new SemanticException(expression_Binary.firstToken, "Expression binary type null");
				
		}
		else
			throw new SemanticException(expression_Binary.firstToken, "Type mismatch between e0 and e1 Expression Binary");
		return expression_Binary;
	}

	
	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = (Expression) expression_Unary.e.visit(this, arg);
		Kind op = expression_Unary.op;
		if (op==Kind.OP_EXCL && (e.TYPE==Type.BOOLEAN || e.TYPE==Type.INTEGER))
			expression_Unary.TYPE=e.TYPE;
		else if ((op==Kind.OP_PLUS || op==Kind.OP_MINUS) && e.TYPE==Type.INTEGER)
			expression_Unary.TYPE= Type.INTEGER;
		else 
			expression_Unary.TYPE=Type.NONE;
		if (expression_Unary.TYPE==Type.NONE)
			throw new SemanticException(expression_Unary.firstToken, "Expression unary null");
		return expression_Unary;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = (Expression) index.e0.visit(this, arg);
		Expression e1 = (Expression) index.e1.visit(this, arg);
		if (e0.TYPE== Type.INTEGER && e1.TYPE==Type.INTEGER) {
			if(e0.getClass().getName() == "cop5556fa17.AST.Expression_PredefinedName" && e1.getClass().getName() == "cop5556fa17.AST.Expression_PredefinedName")
				index.setCartesian(! (e0.firstToken.kind==Kind.KW_r && e1.firstToken.kind==Kind.KW_a));
			else
				index.setCartesian(true);
		}
		else
			throw new SemanticException(index.firstToken, "e0, e1 not integer in Index");
		return index;
	}

	//done
	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//name Index
		 Type t =  ((Declaration) symbolTable.lookupType(expression_PixelSelector.name)).TYPE;
		 if (t==Type.IMAGE) {
			 expression_PixelSelector.TYPE= Type.INTEGER;
			 expression_PixelSelector.index.visit(this, arg);
		 }
		 else { 
			 if (expression_PixelSelector.index==null)
				 expression_PixelSelector.TYPE= t;
			 else {
				 expression_PixelSelector.index.visit(this, arg);
				 expression_PixelSelector.TYPE=Type.NONE;
			 }
		 }
		 if (expression_PixelSelector.TYPE==Type.NONE)
			 throw new SemanticException(expression_PixelSelector.firstToken, "Expression_PixelSelector is null");
		 return expression_PixelSelector;
	}

	//done
	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Expression econd = (Expression) expression_Conditional.condition.visit(this, arg);
		Expression etrue = (Expression) expression_Conditional.trueExpression.visit(this, arg);
		Expression efalse = (Expression) expression_Conditional.falseExpression.visit(this, arg);
		if (econd.TYPE==Type.BOOLEAN && etrue.TYPE==efalse.TYPE) {
			expression_Conditional.TYPE= etrue.TYPE;
		}
		else
			throw new SemanticException(expression_Conditional.firstToken, "Type mismatch in Expression_Conditional");
		return expression_Conditional;
	}

	//done
	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = declaration_Image.name;
		if (symbolTable.lookupType(name)==null) {
			
			declaration_Image.TYPE = Type.IMAGE;
			symbolTable.insert(name, declaration_Image);
		
			if (declaration_Image.xSize!=null) {
				Expression xsize = (Expression) declaration_Image.xSize.visit(this, arg);
				if (declaration_Image.ySize==null) 
					throw new SemanticException(declaration_Image.firstToken, "ysize null in Dec_Image");
				Expression ysize = (Expression) declaration_Image.ySize.visit(this, arg);
				if (xsize.TYPE!=Type.INTEGER || ysize.TYPE!=Type.INTEGER)
					throw new SemanticException(declaration_Image.firstToken, "xsize, ysize not integer in Dec_Image");
			}
			if (declaration_Image.source!=null)
				 declaration_Image.source.visit(this, arg);
		}
		else 	
			throw new SemanticException(declaration_Image.firstToken, "First token not null in Dec_Image");
		return declaration_Image;
	}

	//done
	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String fileorurl = source_StringLiteral.fileOrUrl;
		try {
			URL url = new URL(fileorurl);
			source_StringLiteral.TYPE = Type.URL;
		} catch (MalformedURLException e) {
			source_StringLiteral.TYPE = Type.FILE;
			return source_StringLiteral;
		}
		return source_StringLiteral;
		//throw new UnsupportedOperationException();
	}

	//done
	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Expression param = (Expression) source_CommandLineParam.paramNum.visit(this, arg);
		//source_CommandLineParam.TYPE= param.TYPE; //hw7
		source_CommandLineParam.TYPE=null;
		if (param.TYPE != Type.INTEGER)  //hw6
			throw new SemanticException(source_CommandLineParam.firstToken, "Type not integer in Source_CommandLineParam");
		return source_CommandLineParam;
	}

	//done
	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name= source_Ident.name;
		if (symbolTable.lookupType(name)!=null) {
			Declaration dec = (Declaration) symbolTable.lookupType(name);
			source_Ident.TYPE = dec.TYPE; //check
			if (source_Ident.TYPE!=Type.FILE && source_Ident.TYPE!=Type.URL)
				throw new SemanticException(source_Ident.firstToken, "Type mismatch in Source_Ident");
		}
		else 
			throw new SemanticException(source_Ident.firstToken, "The name is not declared(no Stable entry) in Source_Ident");
		return source_Ident;
	}

	//done
	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = declaration_SourceSink.name;
		if (symbolTable.lookupType(name)==null) {
			
			declaration_SourceSink.setType(TypeUtils.getType(declaration_SourceSink.firstToken));
			symbolTable.insert(name, declaration_SourceSink);
	
			Source source = (Source) declaration_SourceSink.source.visit(this, arg);
			if (source.TYPE!=declaration_SourceSink.TYPE && source.TYPE!=null) //and part hw7
				throw new SemanticException(declaration_SourceSink.firstToken, "Type mismatch between source and declaration_sourcesink");
		}
		else 	
			throw new SemanticException(declaration_SourceSink.firstToken, "First token not null in declaration_sourcesink");
		return declaration_SourceSink;
	}

	
	//done
	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_IntLit.TYPE= Type.INTEGER;
	//	throw new UnsupportedOperationException();
		return expression_IntLit;
	}

	//done
	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = (Expression) expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if (e.TYPE!=Type.INTEGER)
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "Type mismatch in Expression_FunctionAppWithExprArg");
		expression_FunctionAppWithExprArg.TYPE= Type.INTEGER;
		return expression_FunctionAppWithExprArg;
	}

	//done
	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Index i = (Index) expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		expression_FunctionAppWithIndexArg.TYPE= Type.INTEGER;
		return expression_FunctionAppWithIndexArg;
	}

	//done
	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_PredefinedName.TYPE= Type.INTEGER;
		return expression_PredefinedName;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = statement_Out.name;
		if (symbolTable.lookupType(name) !=null){
			statement_Out.setDec((Declaration)symbolTable.lookupType(name));
			Sink s= (Sink) statement_Out.sink.visit(this, arg);
			Declaration dname = (Declaration)symbolTable.lookupType(name);
			if (((dname.TYPE!=Type.INTEGER && dname.TYPE!=Type.BOOLEAN) || s.TYPE!=Type.SCREEN) && (dname.TYPE!=Type.IMAGE || (s.TYPE!=Type.FILE && s.TYPE!=Type.SCREEN)))
				throw new SemanticException(statement_Out.firstToken, "Type mismatch in statementOut");
			
		}
		else 
			throw new SemanticException(statement_Out.firstToken, "name is not declared(no Stable entry) in statementOut");
		return statement_Out;
	}

	//not done
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = statement_In.name;
		if (symbolTable.lookupType(name)!=null) {
			statement_In.setDec((Declaration)symbolTable.lookupType(name));
			//Source s = (Source) statement_In.source.visit(this, arg);
			//Declaration dname = (Declaration)symbolTable.lookupType(name);
			/*if (dname.TYPE!=s.TYPE) 
				throw new SemanticException (statement_In.firstToken, "Type mismatch in statementIn");
			*/  //Assignment 5 update
		}
		else
			throw new SemanticException(statement_In.firstToken, "name is not declared(no Stable entry) in statementIn");
		return statement_In;
	}
	
	//done
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		LHS lhs = (LHS) statement_Assign.lhs.visit(this, arg);
		Expression e = (Expression) statement_Assign.e.visit(this, arg);
		
		if (lhs.TYPE!=e.TYPE) { 
			if(lhs.TYPE==Type.IMAGE && e.TYPE==Type.INTEGER);
			else
				throw new SemanticException(statement_Assign.firstToken, "Type mismatch in statementAssign");
		}
		statement_Assign.setCartesian(lhs.isCartesian); //check this
		return statement_Assign;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = lhs.name;
		if (symbolTable.lookupType(name)!=null) {
			lhs.setDec((Declaration) symbolTable.lookupType(name));
			lhs.TYPE= ((Declaration) symbolTable.lookupType(name)).TYPE;
			if (lhs.index!=null) {
				Index i = (Index) lhs.index.visit(this, arg);
				lhs.isCartesian = i.isCartesian();
			}
			else
				lhs.isCartesian = false;
		}
		else
			throw new SemanticException(lhs.firstToken, "name is not declared(no Stable entry) in LHS");
		return lhs;
	}

	//done
	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_SCREEN.TYPE = Type.SCREEN;
		
		return sink_SCREEN;
	}

	//done
	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name= sink_Ident.name;
		if (symbolTable.lookupType(name)!=null) {
			Declaration dec = (Declaration) symbolTable.lookupType(name);
			sink_Ident.TYPE = dec.TYPE;
			if (sink_Ident.TYPE!=Type.FILE )
				throw new SemanticException(sink_Ident.firstToken, "Type mismatch in sink_Ident");
		}
		else 
			throw new SemanticException(sink_Ident.firstToken, "The name is not declared(no Stable entry) in sink_Ident");
		return sink_Ident;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.TYPE= Type.BOOLEAN;
		return expression_BooleanLit;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = expression_Ident.name;
		if (symbolTable.lookupType(name)!=null) {
			expression_Ident.TYPE= ((Declaration)symbolTable.lookupType(name)).TYPE;
		}
		else
			throw new SemanticException(expression_Ident.firstToken, "name null in Expression_Ident");
		return expression_Ident;
	}

}
