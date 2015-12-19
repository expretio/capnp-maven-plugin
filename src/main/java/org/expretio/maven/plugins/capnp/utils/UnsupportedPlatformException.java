/*
 *=============================================================================
 *                      THIS FILE AND ITS CONTENTS ARE THE
 *                    EXCLUSIVE AND CONFIDENTIAL PROPERTY OF
 *
 *                          EXPRETIO TECHNOLOGIES, INC.
 *
 * Any unauthorized use of this file or any of its parts, including, but not
 * limited to, viewing, editing, copying, compiling, and distributing, is
 * strictly prohibited.
 *
 * Copyright ExPretio Technologies, Inc., 2014. All rights reserved.
 *=============================================================================
 */

package org.expretio.maven.plugins.capnp.utils;

public class UnsupportedPlatformException extends RuntimeException
{
    public UnsupportedPlatformException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UnsupportedPlatformException(String message)
    {
        super(message);
    }

    public UnsupportedPlatformException(Throwable cause)
    {
        super(cause);
    }
}
