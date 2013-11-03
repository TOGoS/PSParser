package togos.psparser;

import togos.lang.ParseError;
import togos.lang.SourceLocation;

public class UnexpectedEOFError extends ParseError
{
	private static final long serialVersionUID = -1331532312738549675L;

	public UnexpectedEOFError(SourceLocation sLoc) {
		super("Unexpected end of file", sLoc);
	}
}
