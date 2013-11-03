package togos.psparser.ast;

import togos.lang.SourceLocation;

public class ApplicationNode extends ASTNode
{
	public final ASTNode operator, operand;
	
	public ApplicationNode( ASTNode operator, ASTNode operand, SourceLocation sLoc ) {
		super(sLoc);
		this.operator = operator;
		this.operand = operand;
	}
	
	@Override public String toSource() {
		return operator.toSourceAtomic() + "(" + (operand instanceof VoidNode ? "" : operand.toSource()) + ")";
	}
	
	@Override public String toSourceAtomic() {
		return toSource();
	}
}
