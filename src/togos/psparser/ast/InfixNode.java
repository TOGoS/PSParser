package togos.psparser.ast;

import togos.lang.SourceLocation;
import togos.psparser.Token;

public class InfixNode extends ASTNode
{
	public final ASTNode operand0, operand1;
	public final Token operator; 
	
	public InfixNode( ASTNode operand0, Token operator, ASTNode operand1, SourceLocation sLoc ) {
		super(sLoc);
		this.operand0 = operand0;
		this.operator = operator;
		this.operand1 = operand1;
	}
	
	public InfixNode( ASTNode operand0, Token operator, ASTNode operand1 ) {
		this( operand0, operator, operand1, operator );
	}
	
	@Override public String toSource() {
		return operand0.toSourceAtomic() + " " + operator.text + " " + operand1.toSourceAtomic();
	}
}
