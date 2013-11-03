package togos.psparser;

import java.io.IOException;

import togos.asyncstream.StreamDestination;
import togos.lang.BaseSourceLocation;
import togos.lang.ScriptError;
import togos.lang.ParseError;
import togos.lang.SourceLocation;

public class Tokenizer implements StreamDestination<CharSequence,ScriptError>
{
	interface TokenizerState {
		TokenizerState end() throws ScriptError;
		TokenizerState data( char c ) throws ScriptError;
	}
	
	class EndTokenizerState implements TokenizerState {
		public TokenizerState end() throws ParseError { throw new ParseError("Unexpected char after end of file", getSourceLocation()); } 
		public TokenizerState data( char c ) throws ParseError { throw new ParseError("Unexpected extra EOF after end of file", getSourceLocation()); }
	}
	
	class BarewordTokenizerState implements TokenizerState {
		StringBuffer word = new StringBuffer();
		int tokenLineNumber, tokenColumnNumber;
		
		public BarewordTokenizerState( char initialChar, String filename, int lineNumber, int columnNumber ) {
			word.append(initialChar);
			this.tokenLineNumber = lineNumber;
			this.tokenColumnNumber = columnNumber;
		}
		
		protected void flush() throws ScriptError {
			tokenStream.data(new Token(QuoteStyle.BAREWORD, word.toString(), filename, tokenLineNumber, tokenColumnNumber));
			word = null;
		}
		
		@Override public TokenizerState data( char c ) throws ScriptError {
			switch( c ) {
			case '(': case ')': case '{': case '}': case '\n': case ',': case ';':
				flush();
				tokenStream.data(new Token(QuoteStyle.BAREWORD, String.valueOf(c), filename, lineNumber, columnNumber));
				return initialTokenizerState;
			case ' ': case '\t': case '\r':
				flush();
				return initialTokenizerState;
			case '"': case '\'':
				throw new ParseError("Unexpected quote in bareword", getSourceLocation());
			default:
				word.append(c);
				return this;
			}
		}
		
		@Override public TokenizerState end() throws ScriptError {
			flush();
			return new EndTokenizerState();
		}
	}
	
	class QuoteTokenizerState implements TokenizerState {
		QuoteStyle qs;
		char endQuoteChar;
		StringBuffer text = new StringBuffer();
		int tokenLineNumber, tokenColumnNumber;
		boolean escaping = false;
		// TODO: Add support for \000, \x00, \u0000, possibly other JS-compatible escape sequence patterns
		
		public QuoteTokenizerState( char endQuoteChar, QuoteStyle qs, String filename, int lineNumber, int columnNumber ) {
			this.endQuoteChar = endQuoteChar;
			this.qs = qs;
			this.tokenLineNumber = lineNumber;
			this.tokenColumnNumber = columnNumber;
		}
		
		@Override public TokenizerState data( char c ) throws ScriptError {
			if( escaping ) {
				switch( c ) {
				case  '0': c = 0x00; break;
				case  'r': c = '\r'; break;
				case  'n': c = '\n'; break;
				case  't': c = '\t'; break;
				case  'f': c = '\f'; break;
				case  'v': c = 0x0B; break;
				case '\'': c = '\''; break;
				case '\\': c = '\\'; break;
				case '\"': c = '\"'; break;
				case '\n': return this;
				}
				escaping = false;
				text.append(c);
				return this;
			} else if( c == '\\' ) {
				escaping = true;
				return this;
			} else if( c == endQuoteChar ) {
				tokenStream.data(new Token(qs, text.toString(), filename, tokenLineNumber, tokenColumnNumber));
				return initialTokenizerState;
			} else {
				text.append(c);
				return this;
			}
		}
		
		@Override public TokenizerState end() throws ScriptError {
			throw new ParseError("Unexpected end of file inside "+qs.name+" string", getSourceLocation());
		}
	}
	
	class InitialTokenizerState implements TokenizerState {
		@Override public TokenizerState end() {
			return new EndTokenizerState();
		}
		
		@Override public TokenizerState data( char c ) throws ScriptError {
			switch( c ) {
			case '(': case ')': case '{': case '}': case '\n': case ',': case ';':
				tokenStream.data(new Token(QuoteStyle.BAREWORD, String.valueOf(c), filename, lineNumber, columnNumber));
				return this;
			case ' ': case '\t': case '\r':
				return this;
			case '"':
				return new QuoteTokenizerState(c, QuoteStyle.DOUBLE_QUOTE, filename, lineNumber, columnNumber);
			case '\'':
				return new QuoteTokenizerState(c, QuoteStyle.SINGLE_QUOTE, filename, lineNumber, columnNumber);
			default:
				return new BarewordTokenizerState(c, filename, lineNumber, columnNumber);
			}
		}
	}
	
	final EndTokenizerState endTokenizerState = new EndTokenizerState();
	final InitialTokenizerState initialTokenizerState = new InitialTokenizerState();
	
	////
	
	protected final TokenHandler<? super Token, ? extends ScriptError> tokenStream;
	protected String filename;
	protected int lineNumber;
	protected int columnNumber;
	protected TokenizerState ts = initialTokenizerState;
	
	////
	
	public Tokenizer( TokenHandler<? super Token, ? extends ScriptError> tokenStream, String filename, int lineNumber, int columnNumber ) {
		this.tokenStream = tokenStream;
		setSourceLocation(filename, lineNumber, columnNumber);
	}
	
	protected void setSourceLocation(String filename, int lineNumber, int columnNumber) {
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}
	
	protected SourceLocation getSourceLocation() {
		return new BaseSourceLocation( filename, lineNumber, columnNumber );
	}
	
	public void data(char c) throws ScriptError {
		ts = ts.data(c);
		switch( c ) {
		case '\n':
			++lineNumber;
			columnNumber = 1;
		default:
			++columnNumber;
		}
	}
	
	@Override public void data(CharSequence value) throws ScriptError {
		for( int i=0; i<value.length(); ++i ) {
			data(value.charAt(i));
		}
	}
	
	@Override public void end() throws ScriptError {
		ts = ts.end();
		tokenStream.end(getSourceLocation());
	}
	
	////
	
	public static void main( String[] args ) throws IOException, ScriptError {
		Tokenizer t = new Tokenizer( new TokenHandler<Token, ScriptError>() {
			@Override public void data( Token value ) throws ScriptError {
				System.err.println( "{{"+value.text+"}} at "+BaseSourceLocation.toString(value) );
			}
			@Override public void end( SourceLocation sLoc ) throws ScriptError {}
		}, "standard input", 1, 1);
		
		for( int c = System.in.read(); c != -1; c = System.in.read() ) {
			t.data( (char)c );
		}
	}
}
