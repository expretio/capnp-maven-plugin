package com.expretio.maven.plugin.capnproto;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import com.expretio.maven.plugins.capnproto.CapnpCompiler;

public class CapnpCompilerTest
{
    private File outputDirectory = new File("target/capnpCompilerTest");
    private File schemaBaseDirectory = new File("src/test/resources/schema");
    private File periodSchema = new File("com/expretio/appia/demand/period.capnp");
    private File marketSchema = new File("com/expretio/appia/core/commercial/market.capnp");

    @Test
    public void test() throws MojoFailureException, MojoExecutionException
    {
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setSchemaBaseDirectory(schemaBaseDirectory)
            .addSchema(periodSchema)
            .addSchema(marketSchema)
            .build();

        compiler.compile();
    }

}
