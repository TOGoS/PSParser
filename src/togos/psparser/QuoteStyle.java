package togos.psparser;

public class QuoteStyle
{
	public static final QuoteStyle BAREWORD = new QuoteStyle("bareword");
	public static final QuoteStyle SINGLE_QUOTE = new QuoteStyle("single-quoted");
	public static final QuoteStyle DOUBLE_QUOTE = new QuoteStyle("double-quoted");
	
	public final String name;
	
	public QuoteStyle( String name ) {
		this.name = name;
	}
}
