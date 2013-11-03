package togos.psparser.ast;

import togos.lang.SourceLocation;

public class VoidNode extends ASTNode
{
	public VoidNode( SourceLocation sLoc ) {
		super(sLoc);
	}
	
	@Override public String toSource() {
		return "()";
	}
}
