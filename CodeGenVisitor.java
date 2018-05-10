package cop5556fa17;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
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
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv ; //added hw5 by me

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		fv = cw.visitField(ACC_STATIC, Kind.KW_x.toString(), "I", null, null);
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, Kind.KW_y.toString(), "I", null, null);
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, Kind.KW_r.toString(), "I", null, null);
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, Kind.KW_a.toString(), "I", null, null);
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, Kind.KW_X.toString(), "I", null, null);
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, Kind.KW_Y.toString(), "I", null, null);
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, Kind.KW_R.toString(), "I", null, null);
		fv.visitEnd();
		fv = cw.visitField(ACC_STATIC, Kind.KW_A.toString(), "I", null, null);
		fv.visitEnd();
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		String fieldName = declaration_Variable.name;
		String fieldType; 
		if (declaration_Variable.type.kind==Kind.KW_boolean) 
			fieldType = "Z";
		else
			fieldType = "I";
		
		fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		fv.visitEnd();
		Expression exp = declaration_Variable.e;
		if (exp != null) {
			exp.visit(this , arg);
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		Expression e1= expression_Binary.e0;
		Expression e2 = expression_Binary.e1;
		e1.visit(this, arg);
		e2.visit(this, arg);
		Kind op =  expression_Binary.op;
		
		if (op == Kind.OP_EQ) {
			 	Label l1 = new Label();
				mv.visitJumpInsn(IF_ICMPNE, l1);
				mv.visitInsn(ICONST_1);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
		}
		//kept the labels the same since it's in if-else block. check if works
		else if (op == Kind.OP_NEQ) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
		}
		else if (op == Kind.OP_LE) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGT, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
		}
		else if (op == Kind.OP_GE) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
		}
		else if (op == Kind.OP_LT) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
		}
		else if (op == Kind.OP_GT) {
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLE, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(l2);
		}
		// plus, minus, div
		else if (op == Kind.OP_PLUS) {
			mv.visitInsn(IADD);
		}
		else if (op ==Kind.OP_MINUS) {
			mv.visitInsn(ISUB);
		}
		else if (op == Kind.OP_DIV) {
			mv.visitInsn(IDIV);
		}
		else if (op == Kind.OP_MOD) {
			mv.visitInsn(IREM);
		}
		else if (op == Kind.OP_TIMES) {
			mv.visitInsn(IMUL);
		}
		//and , or
		else if (op == Kind.OP_AND) {
			mv.visitInsn(IAND);
		}
		else if (op ==Kind.OP_OR) {
			mv.visitInsn(IOR);
		}
		CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getType());
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		Expression unary = expression_Unary.e;
		unary.visit(this, arg);
		Type type = expression_Unary.TYPE;
		if (type == Type.BOOLEAN || type == Type.INTEGER ) {
			if (expression_Unary.op==Kind.OP_MINUS && type == Type.INTEGER) {
				mv.visitInsn(INEG);
			}
			else if (expression_Unary.op==Kind.OP_EXCL) {
				if (type==Type.INTEGER) {
					mv.visitLdcInsn(new Integer(2147483647));
					mv.visitInsn(IXOR);
				}
				else {
					Label l1 = new Label();
					mv.visitJumpInsn(IFEQ, l1);
					mv.visitInsn(ICONST_0);
					Label l2 = new Label();
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l2);
				}
			}
		}
//		throw new UnsupportedOperationException();
		CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getType());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if (!index.isCartesian()) {
			
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
			
			
			/*mv.visitFieldInsn(GETSTATIC, className, Kind.KW_x.toString(), "I");
			mv.visitFieldInsn(GETSTATIC, className, Kind.KW_y.toString(), "I");	*/
		}
/*		else {
			index.e0.visit(this, arg);
			index.e1.visit(this, arg);
		}*/
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, "Ljava/awt/image/BufferedImage;");
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
		Expression e = expression_Conditional.condition;
		e.visit(this, arg);
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		Expression trueExp = expression_Conditional.trueExpression;
		trueExp.visit(this, arg);
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l1);
		Expression falseExp = expression_Conditional.falseExpression;
		falseExp.visit(this, arg);
		mv.visitLabel(l2);
		
//		throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.getType());
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		fv = cw.visitField(ACC_STATIC, declaration_Image.name, ImageSupport.ImageDesc, null, null);
		ImageSupport obj = new ImageSupport();
		if (declaration_Image.source!=null) {
			String s = (String) declaration_Image.source.visit(this, arg);
			mv.visitLdcInsn(s);
			if (declaration_Image.xSize!=null && declaration_Image.ySize!=null) {
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			}
			else 
				{
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
				}
		}
		else {
			if (declaration_Image.xSize!=null && declaration_Image.ySize!=null) {
				 declaration_Image.xSize.visit(this, arg);
				
				 declaration_Image.ySize.visit(this, arg);
				
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
			}
			else 
			{
				visitExpression_PredefinedName(new Expression_PredefinedName(null,Kind.KW_DEF_X), arg);
				visitExpression_PredefinedName(new Expression_PredefinedName(null,Kind.KW_DEF_Y), arg);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
				
			}
			
		}
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		return null;
		//throw new UnsupportedOperationException();
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		mv.visitLdcInsn(new String(source_StringLiteral.fileOrUrl)); //value not variable
		//mv.visitFieldInsn(GETSTATIC, className, source_StringLiteral.fileOrUrl, "Ljava/lang/String;");
		return null;
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		mv.visitVarInsn(ALOAD, 0);
		Expression e = source_CommandLineParam.paramNum;
		if (e!=null) {
			e.visit(this, arg);
			mv.visitInsn(AALOAD);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		fv = cw.visitField(ACC_STATIC, declaration_SourceSink.name, "Ljava/lang/String;", null, null);
		fv.visitEnd();
		if (declaration_SourceSink.source!=null) {
			declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name, "Ljava/lang/String;");
		}
		return null;	
		//throw new UnsupportedOperationException();
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		int value = expression_IntLit.value;
		mv.visitLdcInsn(new Integer(value));
//		throw new UnsupportedOperationException();
	//	CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if (expression_FunctionAppWithExprArg.function== Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}
		else if (expression_FunctionAppWithExprArg.function== Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		//expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		if (expression_FunctionAppWithIndexArg.function==Kind.KW_cart_x) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
		}
		else if (expression_FunctionAppWithIndexArg.function==Kind.KW_cart_y) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		else if (expression_FunctionAppWithIndexArg.function==Kind.KW_polar_a) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}
		else if (expression_FunctionAppWithIndexArg.function==Kind.KW_polar_r) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		if (expression_PredefinedName.kind==Kind.KW_DEF_X) 
			mv.visitLdcInsn(new Integer(256));
		else if (expression_PredefinedName.kind==Kind.KW_DEF_Y) 
			mv.visitLdcInsn(new Integer(256));
		else if (expression_PredefinedName.kind==Kind.KW_Z) 
			mv.visitLdcInsn(new Integer(16777215));
		else 
			mv.visitFieldInsn(GETSTATIC, className,expression_PredefinedName.kind.toString(), "I");
		return null;
		//throw new UnsupportedOperationException();
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		//Sink sink = statement_Out.sink;
		String name = statement_Out.name;
		Type sType=statement_Out.getDec().TYPE;
		String fieldType;
		if (sType==Type.BOOLEAN || sType==Type.INTEGER) {
			if (sType==Type.BOOLEAN)
				fieldType = "Z";
			else
				fieldType = "I";
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, name, fieldType);
			CodeGenUtils.genLogTOS(GRADE, mv, sType);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "("+fieldType+")V", false);
		}
		else if (sType == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			CodeGenUtils.genLogTOS(GRADE, mv, sType);
			statement_Out.sink.visit(this, arg);
		}
		//CodeGenUtils.genLogTOS(GRADE, mv, sType);
		return null;
		//throw new UnsupportedOperationException();
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		Source s= statement_In.source;
		String name = statement_In.name;
		String fieldType="";
		Type sType = statement_In.getDec().TYPE;
		s.visit(this, arg);
		if (sType==Type.INTEGER)
			fieldType = "I";
		else if (sType==Type.BOOLEAN)
			fieldType = "Z";
		if (fieldType.equals("I")) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, name, "I");
		}
		else if (fieldType.equals("Z")) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, name, "Z");
		}
		else {
			if (statement_In.getDec().TYPE==Type.IMAGE) {
				Declaration_Image di = (Declaration_Image) statement_In.getDec();	
				if (di.xSize == null && di.ySize==null) {
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ACONST_NULL);
				}
				else {
					//get x
					mv.visitFieldInsn(GETSTATIC, className, statement_In.name, ImageSupport.ImageDesc);
					mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					//get y
					mv.visitFieldInsn(GETSTATIC, className, statement_In.name, ImageSupport.ImageDesc);
					mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				}
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
				mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, ImageSupport.ImageDesc);
			}
		}
		return null;
			
		//throw new UnsupportedOperationException();
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		//TODO  (see comment)
		if (statement_Assign.lhs.getType() == Type.BOOLEAN || statement_Assign.lhs.getType() == Type.INTEGER) {
			Expression e = statement_Assign.e;
			e.visit(this, arg);
			LHS lhs = statement_Assign.lhs;
			lhs.visit(this, arg);
		}
		else if (statement_Assign.lhs.getType()== Type.IMAGE) {
			
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_X.toString(), "I");
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_Y.toString(), "I");
			if (statement_Assign.isCartesian()) {
				mv.visitInsn(ICONST_0);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_x.toString(), "I");
				Label l3 = new Label();
				mv.visitJumpInsn(GOTO, l3);
				Label l4 = new Label();
				mv.visitLabel(l4);
				//mv.visitLineNumber(63, l4);
				//mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitInsn(ICONST_0);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_y.toString(), "I");
				Label l5 = new Label();
				mv.visitJumpInsn(GOTO, l5);
				Label l6 = new Label();
				mv.visitLabel(l6);
				
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_r.toString(), "I");
				Label l61 = new Label();
				mv.visitLabel(l61);
				//mv.visitLineNumber(65, l7);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_a.toString(), "I");
				Label l62 = new Label();
				mv.visitLabel(l62);
				//mv.visitLineNumber(64, l6);
				//mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				/*mv.visitFieldInsn(GETSTATIC, className, "x", "I");
				mv.visitFieldInsn(GETSTATIC, className, "y", "I");
				mv.visitInsn(IMUL);
				mv.visitVarInsn(ISTORE, 1); */
				statement_Assign.e.visit(this, arg);
				statement_Assign.lhs.visit(this, arg);
				Label l7 = new Label();
				mv.visitLabel(l7);
				//mv.visitLineNumber(63, l7);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IADD);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitLabel(l5);
				//mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_Y.toString(), "I");
				mv.visitJumpInsn(IF_ICMPLT, l6);
				Label l8 = new Label();
				mv.visitLabel(l8);
				//mv.visitLineNumber(62, l8);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IADD);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitLabel(l3);
				//mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_X.toString(), "I");
				mv.visitJumpInsn(IF_ICMPLT, l4);
			}
			else {
				mv.visitInsn(ICONST_0);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_x.toString(), "I");
				Label l3 = new Label();
				mv.visitJumpInsn(GOTO, l3);
				Label l4 = new Label();
				mv.visitLabel(l4);
				//mv.visitLineNumber(63, l4);
				//mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitInsn(ICONST_0);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_y.toString(), "I");
				Label l5 = new Label();
				mv.visitJumpInsn(GOTO, l5);
				Label l6 = new Label();
				mv.visitLabel(l6);
				//mv.visitLineNumber(64, l6);
				//mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_r.toString(), "I");
				Label l7 = new Label();
				mv.visitLabel(l7);
				//mv.visitLineNumber(65, l7);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_a.toString(), "I");
				Label l8 = new Label();
				mv.visitLabel(l8);
				//mv.visitLineNumber(66, l8);
				/*mv.visitFieldInsn(GETSTATIC, "plpOpcodeAss5test/Test", "r", "I");
				mv.visitFieldInsn(GETSTATIC, "plpOpcodeAss5test/Test", "a", "I");
				mv.visitInsn(IMUL);
				mv.visitVarInsn(ISTORE, 1); */
				statement_Assign.e.visit(this, arg);
				statement_Assign.lhs.visit(this, arg);
				Label l9 = new Label();
				mv.visitLabel(l9);
				//mv.visitLineNumber(63, l9);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IADD);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitLabel(l5);
				//mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_y.toString(), "I");
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_Y.toString(), "I");
				mv.visitJumpInsn(IF_ICMPLT, l6);
				Label l10 = new Label();
				mv.visitLabel(l10);
				mv.visitLineNumber(62, l10);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IADD);
				mv.visitFieldInsn(PUTSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitLabel(l3);
				//mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_x.toString(), "I");
				mv.visitFieldInsn(GETSTATIC, className, Kind.KW_X.toString(), "I");
				mv.visitJumpInsn(IF_ICMPLT, l4);
			}
		}
		return null;
		//throw new UnsupportedOperationException();
	} 

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		String fieldType;
		String fieldName = lhs.name;
		if (lhs.TYPE==Type.INTEGER || lhs.TYPE== Type.BOOLEAN) {
			if (lhs.TYPE==Type.INTEGER)
				fieldType = "I";
			else 
				fieldType = "Z";
			Index ind = lhs.index; 
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
			
		}
		else if (lhs.TYPE==Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, ImageSupport.ImageDesc);
			lhs.index.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
		}
		return null;
		//throw new UnsupportedOperationException();
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, "Ljava/lang/String;");
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		boolean val = expression_BooleanLit.value;
		mv.visitLdcInsn(new Boolean(val));
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		String name = expression_Ident.name;
		String fieldType; 
		if (expression_Ident.TYPE==Type.INTEGER || expression_Ident.TYPE==Type.BOOLEAN) {
			if (expression_Ident.TYPE==Type.BOOLEAN)
				fieldType = "Z";
			else
				fieldType = "I";
			mv.visitFieldInsn(GETSTATIC, className, name, fieldType);
		}
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getType());
		return null;
	}

}
