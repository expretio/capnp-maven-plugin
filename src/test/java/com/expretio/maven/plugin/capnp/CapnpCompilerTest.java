package com.expretio.maven.plugin.capnp;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import com.expretio.maven.plugins.capnp.CapnpCompiler;

public class CapnpCompilerTest
{
    private File outputDirectory = new File("target/capnpCompilerTest/output");
    private File schemaBaseDirectory = new File("src/test/resources/schema");
    private File workDirectory = new File("target/capnpCompilerTest/work");

    private String alphaSchema = "com/expretio/maven/plugins/capnp/alpha/alpha.capnp";
    private String betaSchema = "com/expretio/maven/plugins/capnp/beta/beta.capnp";

    @Test
    public void compile() throws MojoFailureException, MojoExecutionException
    {
        // Setting up fixture
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setSchemaBaseDirectory(schemaBaseDirectory)
            .setWorkDirectory(workDirectory)
            .addSchema(alphaSchema)
            .addSchema(betaSchema)
            .build();

        // Exercising system under test
        compiler.compile();

        // Verifying outcome

    }


}
