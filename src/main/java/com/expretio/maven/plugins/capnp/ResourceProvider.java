/*
 *  Copyright (c) 2015 ExPretio Technologies, Inc. and contributors
 *  Licensed under the MIT License:
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.expretio.maven.plugins.capnp;

import static com.google.common.io.Files.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

/**
 * Capnproto resource provider.
 */
public class ResourceProvider
{
    private static final String RESOURCE_PATH = ResourceProvider.class.getPackage().getName().replace('.', '/') + '/';

    private static final String CAPNP = "capnp";
    private static final String CAPNPC_JAVA = "capnpc-java";
    private static final String JAVA_SCHEMA = "java.capnp";

    public static ResourceProvider create(File workDirectory)
    {
        return new ResourceProvider(workDirectory);
    }

    private File workDirectory;
    private File capnp;
    private File capnpcJava;
    private File javaSchema;

    /**
     * Constructor.
     */
    private ResourceProvider(File workDirectory)
    {
        this.workDirectory = workDirectory;
    }

    /**
     * Provides capnproto program.
     */
    public File getCapnp()
        throws IOException
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
    public File getCapnpcJava()
        throws IOException
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
    public File getJavaSchema()
        throws IOException
    {
        if (javaSchema == null)
        {
            javaSchema = getResource(JAVA_SCHEMA);
        }

        return javaSchema;
    }

    protected File getResource(String name)
        throws IOException
    {
        File destFile = new File(workDirectory, name);

        try (
            OutputStream os = asByteSink(destFile).openBufferedStream();
            InputStream is = Resources.getResource(RESOURCE_PATH + name).openStream();
        )
        {
            ByteStreams.copy(is, os);

            destFile.setExecutable(true);
        }

        return destFile;
    }
}
