package org.expretio.maven.plugins.capnp.platform;


public enum Platform
{
    LINUX64( "linux/x64/capnp", "linux/x64/capnpc-java" ),
    WIN32( "windows/x86/capnp.exe", "windows/x64/capnpc-java.exe" );

    private static final String base = "org/expretio/maven/capnp/";

    private String capnp;
    private String capnpcJava;
    private String javaSchema;

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

    public static Platform detect() throws UnsupportedPlatformException
    {
        String osname = System.getProperty( "os.name" ).toLowerCase();
        String osarch = System.getProperty( "os.arch" ).toLowerCase();

        if ( osname.startsWith( "linux" ) && osarch.contains( "64" ) )
        {
            return LINUX64;
        }

        if ( osname.startsWith( "windows" ) )
        {
            return  WIN32;
        }

        throw new UnsupportedPlatformException( "Supported platforms are: " + values() );
    }
}
