package togos.psparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import togos.lang.ParseError;
import togos.lang.ScriptError;
import togos.lang.SourceLocation;
import togos.psparser.ast.ASTNode;
import togos.psparser.ast.ApplicationNode;
import togos.psparser.ast.InfixNode;
import togos.psparser.ast.LiteralStringNode;
import togos.psparser.ast.PhraseNode;
import togos.psparser.ast.PrefixNode;
import togos.psparser.ast.VoidNode;

public class Parser implements TokenHandler<Token, ScriptError>
{
	interface ParseState {
		public ParseState token( Token t ) throws ScriptError;
		public ParseState end( SourceLocation sLoc ) throws ScriptError;
	}
	
	protected static boolean isBareword( Token t, String text ) {
		return t.quoteStyle == QuoteStyle.BAREWORD && text.equals(t.text);
	}
	
	final Map<String,Integer> prefixOperatorPrecedence;
	final Map<String,Integer> infixOperatorPrecedence;
	
	protected boolean isNonWord(String text) {
		return "(".equals(text) || ")".equals(text) ||
			prefixOperatorPrecedence.containsKey(text) ||
			infixOperatorPrecedence.containsKey(text);
	}
	
	// the newline operator will be treated as a special case in that
	// it will be treated as an infix operator only if it is not preceded
	// by another operator.
	
	interface ParseStateNodeHandler {
		public ParseState node( ASTNode n ) throws ScriptError;
	}
	
	// interface ParentParseState extends ParseState, ParseStateNodeHandler { }
	
	/**
	 * Any parse state that expects to read an expression
	 */
	abstract class GeneralParseState implements ParseState {
		final int minPrecedence; // Any operator with precedence below this must be handled by the parent parser state
		final ParseStateNodeHandler parent;
		final ParseStateNodeHandler next;
		
		public GeneralParseState( int minPrecedence, ParseStateNodeHandler parent ) {
			this.minPrecedence = minPrecedence;
			this.parent = parent;
			this.next = new PrePostExpressionParseState(minPrecedence, parent);
		}
		
		@Override public ParseState token( Token t ) throws ScriptError {
			if( t.quoteStyle == QuoteStyle.BAREWORD ) {
				Integer prec;
				if( (prec = prefixOperatorPrecedence.get(t.text)) != null ) {
					return new InitialParseState(prec.intValue()+1, new PrefixOperandNodeHandler(t, next));
				}
				if( "(".equals(t.text) ) {
					return new InitialParseState(0, new ParenValueNodeHandler(next));
				}
				if( ")".equals(t.text) ) {
					return parent.node(new VoidNode(t)).token(t);
				}
				if( "\n".equals(t.text) ) {
					// Ignore at beginning of expressions!
					return this;
				}
				return new PhraseParseState(t, next);
			} else if( t.quoteStyle == QuoteStyle.SINGLE_QUOTE ) {
				return new PhraseParseState(t, next);
			} else if( t.quoteStyle == QuoteStyle.DOUBLE_QUOTE ) {
				return next.node( new LiteralStringNode(t.text, t) );
			}
			throw new RuntimeException("Unrecognized quote style: "+t.quoteStyle.name);
		}
	}
	
	class PrePostExpressionParseState implements ParseStateNodeHandler {
		final int minPrecedence;
		final ParseStateNodeHandler parent;
		
		public PrePostExpressionParseState( int minPrecedence, ParseStateNodeHandler parent ) {
			this.minPrecedence = minPrecedence;
			this.parent = parent;
		}
		
		@Override public ParseState node( ASTNode operand0 ) throws ScriptError {
			return new PostExpressionParseState(operand0, minPrecedence, parent, this);
		}
	}
	
	class PostExpressionParseState implements ParseState {
		final int minPrecedence;
		final ASTNode operand0;
		final ParseStateNodeHandler parent;
		final ParseStateNodeHandler next;
		
		public PostExpressionParseState( ASTNode operand0, int minPrecedence, ParseStateNodeHandler parent, ParseStateNodeHandler next ) {
			this.minPrecedence = minPrecedence;
			this.operand0 = operand0;
			this.parent = parent;
			this.next = next;
		}
		
		@Override public ParseState token( Token t ) throws ScriptError {
			if( t.quoteStyle == QuoteStyle.BAREWORD ) {
				Integer prec;
				if( ")".equals(t.text) ) {
					return parent.node(operand0).token(t);
				} else if( "(".equals(t.text) ) {
					return new InitialParseState(0, new ParenValueNodeHandler(new ApplicationOperandNodeHandler(operand0, next, t)));
				} else if( (prec = infixOperatorPrecedence.get(t.text)) != null ) {
					if( prec.intValue() < minPrecedence ) return parent.node(operand0).token(t);
					return new InitialParseState(prec.intValue()+1, new InfixOperandNodeHandler(operand0, t, next));
				}
			}
			throw new ParseError("Expected an infix operator or end of expression; got "+t, t);
		}
		
		@Override public ParseState end( SourceLocation sLoc ) throws ScriptError {
			return parent.node(operand0).end(sLoc);
		}
	}
	
	class InitialParseState extends GeneralParseState {
		public InitialParseState( int minPrecedence, ParseStateNodeHandler parent ) {
			super( minPrecedence, parent );
		}
		
		@Override public ParseState end( SourceLocation sLoc ) throws ScriptError {
			return parent.node(new VoidNode(sLoc)).end(sLoc);
		}
	}
	
	class PrefixOperandNodeHandler implements ParseStateNodeHandler {
		final Token operator;
		final ParseStateNodeHandler next;
		
		public PrefixOperandNodeHandler( Token operator, ParseStateNodeHandler next ) {
			this.operator = operator;
			this.next = next;
		}
		
		@Override public ParseState node( ASTNode n ) throws ScriptError {
			return next.node(new PrefixNode(operator, n));
		}
	}
	
	class ExpectCloseParenParseState implements ParseState {
		final ASTNode value;
		final ParseStateNodeHandler parent;
		
		public ExpectCloseParenParseState( ASTNode value, ParseStateNodeHandler parent ) {
			this.value = value;
			this.parent = parent;
		}
		
		@Override public ParseState token( Token t ) throws ScriptError {
			if( t.quoteStyle == QuoteStyle.BAREWORD && ")".equals(t.text) ) {
				return parent.node(value);
			} else {
				throw new ParseError("Expected ')', found: "+t, t);
			}
		}
		
		@Override public ParseState end(SourceLocation sLoc) throws ScriptError {
			throw new UnexpectedEOFError(sLoc);
		}
	}
	
	class ParenValueNodeHandler implements ParseStateNodeHandler {
		final ParseStateNodeHandler next;
		
		public ParenValueNodeHandler( ParseStateNodeHandler next ) {
			this.next = next;
		}
		
		@Override public ParseState node( ASTNode value ) throws ScriptError {
			return new ExpectCloseParenParseState( value, next );
		}
	}
	
	class InfixOperandNodeHandler implements ParseStateNodeHandler {
		final ASTNode operand0;
		final Token operator;
		final ParseStateNodeHandler next;
		
		public InfixOperandNodeHandler( ASTNode operand0, Token operator, ParseStateNodeHandler next ) {
			this.operand0 = operand0;
			this.operator = operator;
			this.next = next;
		}
		
		@Override public ParseState node( ASTNode operand1 ) throws ScriptError {
			return next.node(new InfixNode(operand0, operator, operand1, operator));
		}
	}
	
	class PhraseParseState implements ParseState {
		final ParseStateNodeHandler parent;
		final List<String> words = new ArrayList<String>(10);
		final SourceLocation sLoc;
		
		public PhraseParseState( Token firstWord, ParseStateNodeHandler parent ) {
			this.parent = parent;
			this.words.add(firstWord.text);
			this.sLoc = firstWord;
		}
		
		protected PhraseNode toNode() {
			return new PhraseNode(words.toArray(new String[words.size()]), sLoc);
		}
		
		@Override public ParseState token( Token t ) throws ScriptError {
			if( t.quoteStyle == QuoteStyle.BAREWORD && isNonWord(t.text) ) {
				return parent.node(toNode()).token(t);
			} else if( t.quoteStyle == QuoteStyle.DOUBLE_QUOTE ) {
				throw new ParseError("Unexpected double-quoted string after phrase", t);
			} else {
				words.add(t.text);
				return this;
			}
		}
		
		@Override public ParseState end( SourceLocation sLoc ) throws ScriptError {
			return parent.node(toNode()).end(sLoc);
		}
	}
	
	class ApplicationOperandNodeHandler implements ParseStateNodeHandler {
		final ASTNode operator;
		final ParseStateNodeHandler next;
		final SourceLocation sLoc;
		
		public ApplicationOperandNodeHandler( ASTNode operator, ParseStateNodeHandler parent, SourceLocation sLoc ) {
			this.operator = operator;
			this.next = parent;
			this.sLoc = sLoc;
		}
		
		@Override public ParseState node( ASTNode operand ) throws ScriptError {
			return next.node(new ApplicationNode(operator, operand, sLoc));
		}
	}
	
	////
	
	ParseStateNodeHandler rootNodeHandler = new ParseStateNodeHandler() {
		public ParseState node(ASTNode n) throws ScriptError {
			System.err.println(n);
			return rootParseState;
		};
	};
	
	class EndParseState implements ParseState {
		@Override public ParseState end( SourceLocation sLoc ) throws ScriptError {
			throw new ParseError("Unexpected EOF after EOF", sLoc);
		}
		
		@Override public ParseState token( Token t ) throws ScriptError {
			throw new ParseError("Unexpected EOF after EOF", t);
		}

	}
	
	InitialParseState rootParseState = new InitialParseState(6, rootNodeHandler) {
		@Override public ParseState end( SourceLocation sLoc ) throws ScriptError {
			return new EndParseState();
		}
	};
	
	public Parser( Map<String,Integer> prefixOperatorPrecedence, Map<String,Integer> infixOperatorPrecedence ) {
		this.prefixOperatorPrecedence = prefixOperatorPrecedence;
		this.infixOperatorPrecedence = infixOperatorPrecedence;
	}
	
	ParseState ps = rootParseState;
	
	@Override public void data( Token value ) throws ScriptError {
		ps = ps.token( value );
	}

	@Override public void end( SourceLocation sLoc ) throws ScriptError {
		ps = ps.end( sLoc );
	}
	
	////
	
	public static void main( String[] args ) throws IOException, ScriptError {
		Parser p = new Parser( Collections.<String,Integer>emptyMap(), StandardOperators.PRECEDENCE );
		Tokenizer t = new Tokenizer( p, "standard input", 1, 1);
		
		t.data("foo(bar : thingy, baz : thingy, quux : snood)\n");
		t.data("a + b + 123 * 456 + 789");
		t.end();
		/*
		for( int c = System.in.read(); c != -1; c = System.in.read() ) {
			t.data( (char)c );
		}
		*/
	}
}
