package togos.psparser;

import togos.lang.SourceLocation;

public class Token implements SourceLocation
{
	QuoteStyle quoteStyle;
	String text;
	
	public final String sourceFilename;
	public final int sourceLineNumber, sourceColumnNumber;
	
	public Token( QuoteStyle quoteStyle, String text, String sourceFilename, int sourceLineNumber, int sourceColumnNumber ) {
		this.quoteStyle = quoteStyle;
		this.text = text;
		this.sourceFilename = sourceFilename;
		this.sourceLineNumber = sourceLineNumber;
		this.sourceColumnNumber = sourceColumnNumber;
	}
	
	@Override public String getSourceFilename() { return sourceFilename; }
	@Override public int getSourceLineNumber() { return sourceLineNumber; }
	@Override public int getSourceColumnNumber() { return sourceColumnNumber; }
}
