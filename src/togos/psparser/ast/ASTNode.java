package togos.psparser.ast;

import togos.lang.SourceLocation;

public abstract class ASTNode implements SourceLocation
{
	public final String sourceFilename;
	public final int sourceLineNumber, sourceColumnNumber;
	
	public ASTNode( SourceLocation sLoc ) {
		this.sourceFilename = sLoc.getSourceFilename();
		this.sourceLineNumber = sLoc.getSourceLineNumber();
		this.sourceColumnNumber = sLoc.getSourceColumnNumber();
	}
	
	@Override public String getSourceFilename() { return sourceFilename; }
	@Override public int getSourceLineNumber() { return sourceLineNumber; }
	@Override public int getSourceColumnNumber() { return sourceColumnNumber; }
	
	public abstract String toSource();
	
	public String toSourceAtomic() {
		return "(" + toString() + ")";
	}
	
	public String toString() { return toSource(); }
}
