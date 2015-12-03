package com.expretio.maven.plugins.capnproto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

import com.google.common.io.Files;


/**
 * Capnproto resource provider.
 */
public class ResourceProvider
{
    private static final String CAPNP = "capnp";
    private static final String CAPNPC_JAVA = "capnpc-java";
    private static final String JAVA_SCHEMA = "java.capnp";

    public static ResourceProvider create()
    {
        return new ResourceProvider();
    }

    private File tempDirectory;
    private File capnp;
    private File capnpcJava;
    private File javaSchema;

    private ResourceProvider(){}

    /**
     * Provides capnproto program.
     */
    public File getCapnp() throws MojoExecutionException
    {
        if (capnp == null)
        {
            capnp = getResource(CAPNP);
        }

        return capnp;
    }

    /**
     * Provides capnproto java plugin.
     */
    public File getCapnpcJava() throws MojoExecutionException
    {
        if (capnpcJava == null)
        {
            capnpcJava = getResource(CAPNPC_JAVA);
        }

        return capnpcJava;
    }

    /**
     * Provides java schema.
     */
    public File getJavaSchema() throws MojoExecutionException
    {
        if (javaSchema == null)
        {
            javaSchema = getResource(JAVA_SCHEMA);
        }

        return javaSchema;
    }

    // [Utility methods]

    public File getResource(String name) throws MojoExecutionException
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        File file = new File(getTempDirectory(), name);
        file.deleteOnExit();

        try(OutputStream os = new FileOutputStream(file);
            InputStream is = cl.getResourceAsStream(name);)
        {
            IOUtils.copy(is, os);

            file.setExecutable(true);
        }
        catch(IOException ioe)
        {
            throw new MojoExecutionException("Cannot copy program file:" + ioe.getMessage());
        }

        return file;
    }

    private File getTempDirectory() throws MojoExecutionException
    {
        if (tempDirectory == null)
        {
            tempDirectory = Files.createTempDir();
            tempDirectory.deleteOnExit();
        }

        return tempDirectory;
    }
}
