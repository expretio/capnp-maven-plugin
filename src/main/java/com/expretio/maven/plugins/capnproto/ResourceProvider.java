package com.expretio.maven.plugins.capnproto;

import java.io.File;

/**
 * Capnproto resource provider.
 */
public class ResourceProvider
{
    private static final String RESOURCE_DIR = "src/main/resources/";
    private static final String CAPNP = RESOURCE_DIR + "capnp";
    private static final String CAPNPC_JAVA = RESOURCE_DIR + "capnpc-java";
    private static final String JAVA_SCHEMA = RESOURCE_DIR + "java.capnp";

    public static ResourceProvider create()
    {
        return new ResourceProvider();
    }

    private ResourceProvider(){}

    /**
     * Provides capnproto program.
     */
    public File getCapnp()
    {
        return new File(CAPNP);
    }

    /**
     * Provides capnproto java plugin.
     */
    public File getCapnpcJava()
    {
        return new File(CAPNPC_JAVA);
    }

    /**
     * Provides java schema.
     */
    public File getJavaSchema()
    {
        return new File(JAVA_SCHEMA);
    }

    /**
     * Provides parent directory of java schema file.
     */
    public File getJavaSchemaDirectory()
    {
        return new File(JAVA_SCHEMA).getParentFile();
    }

}
