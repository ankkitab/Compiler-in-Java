/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
  */

package cop5556fa17;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}
	//adding State enum
		public static enum State {
			START, IN_DIGIT, IN_IDENTI, IN_STRING, IN_COMMENT;
		}
		
		public HashMap<String,Kind> keywords = new HashMap<>();
		public HashMap<String,Kind> boolLit = new HashMap<>();
		public void insertAllKeys() {
			keywords.put("x", Kind.KW_x); keywords.put("X", Kind.KW_X);
			keywords.put("y", Kind.KW_y); keywords.put("Y", Kind.KW_Y);
			keywords.put("r", Kind.KW_r);keywords.put("R", Kind.KW_R);
			keywords.put("a", Kind.KW_a);keywords.put("A", Kind.KW_A);
			keywords.put("Z", Kind.KW_Z); keywords.put("DEF_X", Kind.KW_DEF_X); 
			keywords.put("DEF_Y", Kind.KW_DEF_Y); keywords.put("SCREEN",Kind.KW_SCREEN); 
			keywords.put("cart_x", Kind.KW_cart_x); keywords.put("cart_y", Kind.KW_cart_y);
			keywords.put("polar_a", Kind.KW_polar_a); keywords.put("polar_r", Kind.KW_polar_r);
			keywords.put("abs", Kind.KW_abs);keywords.put("sin", Kind.KW_sin); 
			keywords.put("cos",Kind.KW_cos); keywords.put("atan", Kind.KW_atan);keywords.put("log",Kind.KW_log);
			keywords.put("image",Kind.KW_image);keywords.put("int",Kind.KW_int);
			keywords.put("boolean", Kind.KW_boolean);keywords.put("url", Kind.KW_url);
			keywords.put("file", Kind.KW_file);
			
			boolLit.put("true", Kind.BOOLEAN_LITERAL); boolLit.put("false", Kind.BOOLEAN_LITERAL);
			
		}
	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  



	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
	}
	
	public boolean isEscape(char ch) {
		boolean flag= false;
		switch (ch) {
			case 'b': {
				flag = true;
				break;
			}
			case 't': {
				flag = true;
				break;
			}
			case 'n': {
				flag = true;
				break;
			}
			case 'f': {
				flag = true;
				break;
			}
			case 'r': {
				flag = true;
				break;
			}
			case '\\': {
				flag = true;
				break;
			}
			case '"': {
				flag = true;
				break;
			}
			case '\'': {
				flag = true;
				break;
			} 
		}
		return flag;
	}
	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO  Replace this with a correct and complete implementation!!! */
		insertAllKeys();
		int pos =0;
		int line = 1;
		int posInLine = 1;
		int len=1;
		State state= State.START;
		int startpos =0;
		boolean strOpen = false;
		boolean errorFlag = false; 
		boolean earlyEOF = false;
		String errormsg="";
		StringBuilder intLiteral = new StringBuilder();
		StringBuilder identi = new StringBuilder();
		char prev = '\0';
		while (pos<=chars.length-1) {
			char ch= chars[pos];
			switch (state) {
			
			case START : {
				ch= chars[pos];
				startpos = pos;
				switch(ch) {
				//separators
				case ';' : {
								tokens.add(new Token(Kind.SEMI, startpos, 1, line, posInLine));
								pos++;
								posInLine++;
								break;
							}
				case ',' : {
							tokens.add(new Token(Kind.COMMA, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case '(' : {
							tokens.add(new Token(Kind.LPAREN, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case ')' : {
							tokens.add(new Token(Kind.RPAREN, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case '[' : {
							tokens.add(new Token(Kind.LSQUARE, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case ']' : {
							tokens.add(new Token(Kind.RSQUARE, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				//separators
				
				case '\n' : {
								pos++;
								line++;
								posInLine=1;
								break;
							}
				case '\r' :{	
								pos++;
								if (chars[pos]=='\n')
									pos++;
								line++;
								posInLine=1;
								break;
							}
				case '"' : {
								strOpen = true;
								state = State.IN_STRING;
								pos++;
								break;
				}
				//operators 
				case '?' : {
							tokens.add(new Token(Kind.OP_Q, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case ':' : {
							tokens.add(new Token(Kind.OP_COLON, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case '&' : {
							tokens.add(new Token(Kind.OP_AND, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case '|' : {
							tokens.add(new Token(Kind.OP_OR, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case '+' : {
							tokens.add(new Token(Kind.OP_PLUS, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case '/' : {
							if (chars[pos+1]=='/') {
								pos++;
								state=State.IN_COMMENT;
								break;
							}
							tokens.add(new Token(Kind.OP_DIV, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case '%' : {
							tokens.add(new Token(Kind.OP_MOD, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				case '@' : {
							tokens.add(new Token(Kind.OP_AT, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
				}
				//double line ones
				case '=' : {
							if (prev=='=' || prev=='<'|| prev=='>'|| prev=='!') {
								startpos-=1;
								switch(prev) {
								case '=' : {
									tokens.add(new Token(Kind.OP_EQ, startpos, 2, line, posInLine));
									pos++;
									posInLine+=2;
									len=1;
									break;
								}
								case '<' : {
									tokens.add(new Token(Kind.OP_LE, startpos, 2, line, posInLine));
									pos++;
									posInLine+=2;
									len=1;
									break;
								}
								case '>' : {
									tokens.add(new Token(Kind.OP_GE, startpos, 2, line, posInLine));
									pos++;
									posInLine+=2;
									len=1;
									break;
								}
								case '!' :{
									tokens.add(new Token(Kind.OP_NEQ, startpos, 2, line, posInLine));
									pos++;
									posInLine+=2;
									len=1;
									break;
								}	
								}
								prev='\0';
								 
							}
							else {
								if (chars[pos+1]=='=') {
									len++;
									pos++;
									prev='=';
								}
								else {
									tokens.add(new Token(Kind.OP_ASSIGN, startpos, 1, line, posInLine));
									pos++;
									posInLine++;
								}
							}
							break;
				}
				
				case '>' : {
						if (prev=='-') {
							startpos-=1;
							tokens.add(new Token(Kind.OP_RARROW, startpos, 2, line, posInLine));
							pos++;
							posInLine+=2;
							len=1;
							prev='\0';
							break;
						}
						if (chars[pos+1]=='=') {
							len++;
							pos++;
							prev = '>';
						}
						else {
							tokens.add(new Token(Kind.OP_GT, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
				}
				
				case '<' : {
						if (chars[pos+1]=='=' || chars[pos+1]=='-') {
							len++;
							pos++;
							prev = '<';
						}
						else {
							tokens.add(new Token(Kind.OP_LT, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
				}
				
				case '!' : {
						if (chars[pos+1]=='=' ) {
							len++;
							pos++;
							prev = '!';
						}
						else {
							tokens.add(new Token(Kind.OP_EXCL, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
				}
				case '-' : {
						if (prev=='<') {
							startpos-=1;
							tokens.add(new Token(Kind.OP_LARROW, startpos, 2, line, posInLine));
							pos++;
							posInLine+=2;
							len=1;
							prev='\0';
							break;
						}
						if (chars[pos+1]=='>' ) {
							len++;
							pos++;
							prev = '-';
						}
						else {
							tokens.add(new Token(Kind.OP_MINUS, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
				}
				
				case '*' : {
						if (prev=='*') {
							startpos-=1;
							tokens.add(new Token(Kind.OP_POWER, startpos, 2, line, posInLine));
							pos++;
							posInLine+=2;
							len=1;
							prev='\0';
							break;
						}
						if (chars[pos+1]=='*' ) {
							len++;
							pos++;
							prev = '*';
						}
						else {
							tokens.add(new Token(Kind.OP_TIMES, startpos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
				}
				//operators end here
				
				default : {
							if (Character.isDigit(ch)) {
								if (ch=='0') {
									tokens.add(new Token(Kind.INTEGER_LITERAL,startpos,1,line,posInLine));
									posInLine++;
								}
								else {
									intLiteral.append(ch);
									state = State.IN_DIGIT;
								}
								pos++;
							}
							else if (Character.isJavaIdentifierStart(ch)){
								identi.append(ch);
								state = State.IN_IDENTI;
								pos ++; 
							}
							else if (Character.isWhitespace(ch)) {
								pos++;
								posInLine++;
							}
							else {
									if (pos==chars.length-1)
										pos++;
									else{
										pos++;
										errorFlag=true;
										errormsg="Illegal character";
										break;
									}
							}
				} 
				} //switch(ch) ends here
				break;
			}//switch(start) ends here
			
			case IN_COMMENT : {
								ch = chars[pos];
								if (ch=='\n' || ch=='\r') {
									line++;
									pos++;
									if (ch=='\r' && chars[pos]=='\n')
										pos++;
									posInLine=1;
									state= State.START;
									break;
								}
								else 
								{
									pos++;
								}
								break;
			}
			
			case IN_STRING :  { 
								ch = chars[pos];
								//escape
								switch (ch) {
								case '"' : {
										len++;
										tokens.add(new Token(Kind.STRING_LITERAL, startpos,len,line, posInLine));
										prev='\0';
										posInLine+=len;
										len=1;
										pos++;
										state = State.START;
										strOpen= false;
										break;
								} 
								case '\\': {
											char nex= chars[pos+1];
											if (!isEscape(nex)) {
												errorFlag=true;
												errormsg = "String Literal not valid";
												break;
											}
											else{
												pos+=2;
												len+=2;
											}
											break;
								}
								//added here after 1st submission
								case '\n': {
									errorFlag=true;
									errormsg = "String Literal not valid";
									pos++; //as it decrease by 1 later
									break;
								}
								case '\r': {
									errorFlag=true;
									errormsg = "String Literal not valid";
									pos++;
									break;
								}
								//ended here
								//check for escape sequence
								default : {
										pos++;
										len++;
										break;
								}
							
							}
							break;
				
					} // in_str ends
			
			case IN_IDENTI: {
							ch = chars[pos];
							if (newIdentifierPart(ch)) {
								identi.append(ch);
								pos++;
								len++;
							}
							else {
								if (keywords.get(identi.toString())!=null) 
									tokens.add(new Token(keywords.get(identi.toString()), startpos, len, line, posInLine));
								else if (boolLit.get(identi.toString())!=null) 
									tokens.add(new Token(boolLit.get(identi.toString()), startpos, len, line, posInLine));
								else
									tokens.add(new Token(Kind.IDENTIFIER, startpos, len, line, posInLine));
								posInLine+=len;
								len=1;
								state=State.START;
								identi = new StringBuilder();
								
							}
							break;
					}
			
			case IN_DIGIT : {	
								if (Character.isDigit(ch)) {
									intLiteral.append(ch);
									pos++;
									len++;
								}
								else {
									try{
									int val = Integer.parseInt(intLiteral.toString());
									}
									catch (NumberFormatException e) {
										errorFlag=true;
										pos=startpos+1;
										errormsg= "Number Format Exception";
										break;
									}
									tokens.add(new Token(Kind.INTEGER_LITERAL, startpos, len, line, posInLine));	
									posInLine+=len;
									len=1;
									//adding here after 1st submission for 2nd error
									intLiteral = new StringBuilder();
									//ends here
									state=State.START;
								}
								break;
							}
						
			} //switch(state) ends here
			if (errorFlag)
				break;
		}// while ends
		if (strOpen || errorFlag) {
			if (strOpen && !errorFlag) 
				errormsg = "String Literal Error";
			throw new LexicalException("Lexical Exception - "+errormsg, pos-1);
		} //edit:after1st increase pos by 1 inside the switch always as I decrease by 1 here anyway
		tokens.add(new Token(Kind.EOF, pos-1, 0, line, posInLine));
		return this;

	}
	
	public boolean newIdentifierPart(char ch) {
		boolean flag= false;
		if (Character.isJavaIdentifierStart(ch) || Character.isDigit(ch)) 
			flag = true;
		return flag;
	}

	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
