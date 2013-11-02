package togos.psparser;

import java.util.Map;
import java.util.Set;

import togos.asyncstream.StreamDestination;
import togos.lang.ScriptError;
import togos.psparser.ast.ASTNode;

public class Parser implements StreamDestination<Token, ScriptError>
{
	interface ParseState {
		public ParseState data( Token t ) throws ScriptError;
		public ParseState end() throws ScriptError;
	}
	
	protected static boolean isBareword( Token t, String text ) {
		return t.quoteStyle == QuoteStyle.BAREWORD && text.equals(t.text);
	}
	
	Set<String> prefixOperators;
	Map<String,Integer> infixOperatorPrecedence;
	
	// the newline operator will be treated as a special case in that
	// it will be treated as an infix operator only if it is not preceded
	// by another operator.
	
	interface ParentParserState extends ParseState {
		public ParseState data( ASTNode n ) throws ScriptError;
	}
	
	// class PrefixOperationParseState 
	
	class ASTParseState implements ParseState {
		int minPrecedence; // Any operator with precedence below this must be handled by the parent parser state
		StreamDestination<ASTNode,ScriptError> nodeDest;
		
		@Override public ParseState data( Token t ) throws ScriptError {
			if( t.quoteStyle == QuoteStyle.BAREWORD ) {
				if( prefixOperators.contains(t.text) ) {
					// TODO
				}
				// TODO
			} else {
				// TODO
			}
			throw new UnsupportedOperationException();
		}
		
		@Override public ParseState end() throws ScriptError {
			throw new UnsupportedOperationException();
		}
	}
	
	ParseState ps;
	
	@Override
	public void data( Token value ) throws ScriptError {
		ps = ps.data( value );
		throw new UnsupportedOperationException();
	}

	@Override
	public void end() throws ScriptError {
		ps = ps.end();
		throw new UnsupportedOperationException();
	}
}
