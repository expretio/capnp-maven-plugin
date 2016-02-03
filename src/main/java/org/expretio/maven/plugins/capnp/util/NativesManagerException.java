package org.expretio.maven.plugins.capnp.util;

public class NativesManagerException
    extends RuntimeException
{
    public NativesManagerException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NativesManagerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NativesManagerException(String message)
    {
        super(message);
    }

    public NativesManagerException(Throwable cause)
    {
        super(cause);
    }
}
