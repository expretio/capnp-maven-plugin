package org.expretio.maven.plugins.capnp.platform;

import com.google.common.base.StandardSystemProperty;

public enum Platform
{
    LINUX64( "linux/x64/capnp", "linux/x64/capnpc-java" ),
    OSX64( "osx/x64/capnp", "osx/x64/capnpc-java" ),
    WIN32( "windows/x86/capnp.exe", "windows/x86/capnpc-java.exe" ),
    UNSUPPORTED( null, null );

    private static final Platform CURRENT_PLATFORM = detect();

    private static final String base = "org/expretio/maven/capnp/";

    private final String capnp;
    private final String capnpcJava;
    private final String javaSchema;

    private Platform( String capnp, String capnpcJava )
    {
        this.capnp = base + "compiler/" + capnp;
        this.capnpcJava = base + "javaplugin/" + capnpcJava;
        this.javaSchema = base + "javaplugin/java.capnp";
    }

    public String getClassifier()
    {
        return this.name().toLowerCase();
    }

    /**
     * Returns native compiler path.
     */
    public String getCapnp()
    {
        return capnp;
    }

    /**
     * Returns java-plugin path.
     */
    public String getCapnpcJava()
    {
        return capnpcJava;
    }

    /**
     * Returns java-plugin java.capnp path.
     */
    public String getJavaSchema()
    {
        return javaSchema;
    }

    public static Platform getCurrent()
    {
        return CURRENT_PLATFORM;
    }

    protected static Platform detect()
    {
        String osName = StandardSystemProperty.OS_NAME.value().toLowerCase();
        String osArch = StandardSystemProperty.OS_ARCH.value().toLowerCase();

        if ( osName.startsWith( "linux" ) && osArch.contains( "amd64" ) )
        {
            return LINUX64;
        }

        if ( osName.startsWith( "mac os x" ) && osArch.contains( "64" ) )
        {
            return OSX64;
        }

        if ( osName.startsWith( "windows" ) )
        {
            return WIN32;
        }

        return UNSUPPORTED;
    }
}
