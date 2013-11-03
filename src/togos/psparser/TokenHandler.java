package togos.psparser;

import togos.lang.SourceLocation;

public interface TokenHandler<T extends SourceLocation, E extends Throwable>
{
	public void data( T t ) throws E;
	public void end( SourceLocation sLoc ) throws E;
}
