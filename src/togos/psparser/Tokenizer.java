package togos.psparser;

import togos.asyncstream.StreamDestination;
import togos.lang.ScriptError;

public class Tokenizer implements StreamDestination<CharSequence,ScriptError>
{
	protected final StreamDestination<Token,ScriptError> tokenStream;
	protected String filename;
	protected int lineNumber;
	protected int columnNumber;
	
	public Tokenizer( StreamDestination<Token,ScriptError> tokenStream, String filename, int lineNumber, int columnNumber ) {
		this.tokenStream = tokenStream;
		setSourceLocation(filename, lineNumber, columnNumber);
	}
	
	protected void setSourceLocation(String filename, int lineNumber, int columnNumber) {
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}
	
	@Override public void data(CharSequence value) throws ScriptError {
		throw new UnsupportedOperationException();
	}

	@Override public void end() throws ScriptError {
		throw new UnsupportedOperationException();
	}
}
