package togos.psparser.ast;

import togos.lang.SourceLocation;

public class LiteralStringNode extends ASTNode
{
	public final String text;

	public LiteralStringNode( String text, SourceLocation sLoc ) {
		super(sLoc);
		this.text = text;
	}
	
	@Override public String toSource() {
		return '"' + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}
	
	@Override public String toSourceAtomic() {
		return toSource();
	}
}
