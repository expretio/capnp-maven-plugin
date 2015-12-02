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
    private File alternativeSchema = new File("com/expretio/appia/demand/alternative/alternative.capnp");
    private File terminalSchema = new File("com/expretio/appia/demand/alternative/terminal.capnp");
    private File preferenceListSchema = new File("com/expretio/appia/demand/profile/preference_list.capnp");

    @Test
    public void test() throws MojoFailureException, MojoExecutionException
    {
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setSchemaBaseDirectory(schemaBaseDirectory)
            .addSchema(alternativeSchema)
            .addSchema(terminalSchema)
            .addSchema(preferenceListSchema)
            .build();

        compiler.compile();
    }

}
