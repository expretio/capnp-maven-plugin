package org.expretio.maven.plugins.capnp.util;

import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Maps;
import com.sun.jna.Platform;

public class JavaPlatform
{
    protected static Method getCanonicalArchitectureMethod;

    static
    {
        try
        {
            getCanonicalArchitectureMethod =
                    Platform.class.getDeclaredMethod( "getCanonicalArchitecture", String.class );
        }
        catch ( Exception e )
        {
            throw new ExceptionInInitializerError( e );
        }

        getCanonicalArchitectureMethod.setAccessible( true );
    }

    public static enum Os
    {
        AIX( Platform.AIX ),
        ANDROID( Platform.ANDROID ),
        FREEBSD( Platform.FREEBSD ),
        GNU( Platform.GNU ),
        LINUX( Platform.LINUX ),
        OSX( Platform.MAC ),
        NETBSD( Platform.NETBSD ),
        OPENBSD( Platform.OPENBSD ),
        SOLARIS( Platform.SOLARIS ),
        WINDOWS( Platform.WINDOWS ),
        UNKNOWN( Platform.UNSPECIFIED );

        private static final Map<Integer, Os> osIdMap = Maps.newHashMap();

        static
        {
            for ( Os os : Os.values() )
            {
                osIdMap.put( os.osId, os );
            }
        }

        private int osId;

        private Os( int osId )
        {
            this.osId = osId;
        }

        public static Os getOs( String name )
        {;
            return Os.valueOf( name.toUpperCase() );
        }

        public static Os getCurrentOs()
        {
            return osIdMap.get( Platform.getOSType() );
        }
    }

    private JavaPlatform() {}

    public static Os getOs( String name )
    {
        return Os.getOs( name );
    }

    public static Os getCurrentOs()
    {
        // XXX: cache this;
        return Os.getCurrentOs();
    }

    public static String getCurrentArch()
    {
        // XXX: cache this;
        return getCanonicalArchitecture( StandardSystemProperty.OS_ARCH.value() );
    }

    public static String getCanonicalArchitecture( String archName )
    {
        try
        {
            return (String) getCanonicalArchitectureMethod.invoke( null, archName );
        }
        catch ( Exception e )
        {
            // should never happen
            throw new AssertionError( e );
        }
    }
}
