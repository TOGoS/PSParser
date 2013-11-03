package togos.psparser.ast;

import togos.lang.SourceLocation;

public class PhraseNode extends ASTNode
{
	public final String[] words;
	
	public PhraseNode( String[] words, SourceLocation sLoc ) {
		super(sLoc);
		this.words = words;
	}
	
	protected static String quoteWord( String word ) {
		return "'" + word.replace("\\", "\\\\").replace("'", "\\'") + "'";
	}
	
	@Override public String toSource() {
		StringBuilder sb = new StringBuilder();
		for( int i=0; i<words.length; ++i ) {
			if( i > 0 ) sb.append(" ");
			sb.append(quoteWord(words[i]));
		}
		return sb.toString();
	}
	
	@Override public String toSourceAtomic() {
		return toSource();
	}
}
