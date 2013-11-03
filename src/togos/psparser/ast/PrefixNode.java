package togos.psparser.ast;

import togos.psparser.Token;

public class PrefixNode extends ASTNode
{
	public final Token operator;
	public final ASTNode operand;
	
	public PrefixNode( Token operator, ASTNode operand ) {
		super( operator );
		this.operator = operator;
		this.operand = operand;
	}
	
	@Override public String toSource() {
		return operator + " " + operand;
	}
}
