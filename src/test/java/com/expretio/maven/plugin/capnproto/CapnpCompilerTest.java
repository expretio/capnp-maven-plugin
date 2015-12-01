package com.expretio.maven.plugin.capnproto;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import com.expretio.maven.plugins.capnproto.CapnpCompiler;

public class CapnpCompilerTest
{
    private String outputDirectory = "target/capnpCompilerTest";
    private String schemaBaseDirectory = "src/test/resources/schema";
    private String periodSchema = "com/expretio/appia/demand/period.capnp";
    private String marketSchema = "com/expretio/appia/core/commercial/market.capnp";


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
