package com.expretio.maven.plugin.capnp;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.assertj.core.api.Assertions;
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
        assertThat(workDirectory)
            .contains("com/expretio/maven/plugins/capnp/alpha/alpha.capnp")
            .contains("com/expretio/maven/plugins/capnp/beta/beta.capnp")
            .contains("capnp")
            .contains("capnpc-java")
            .contains("java.capnp");

        assertThat(outputDirectory)
            .contains("com/expretio/maven/plugins/capnp/alpha/AlphaCapnp.java")
            .contains("com/expretio/maven/plugins/capnp/beta/BetaCapnp.java");
    }

    @Test(expected = MojoFailureException.class)
    public void withoutOutputDirectory() throws MojoFailureException, MojoExecutionException
    {
        // Setting up fixture
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setSchemaBaseDirectory(schemaBaseDirectory)
            .setWorkDirectory(workDirectory)
            .addSchema(alphaSchema)
            .addSchema(betaSchema)
            .build();

        // Exercising system under test
        compiler.compile();
    }

    @Test(expected = MojoFailureException.class)
    public void withoutSchemaBaseDirectory() throws MojoFailureException, MojoExecutionException
    {
        // Setting up fixture
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setWorkDirectory(workDirectory)
            .addSchema(alphaSchema)
            .addSchema(betaSchema)
            .build();

        // Exercising system under test
        compiler.compile();
    }

    @Test(expected = MojoFailureException.class)
    public void withoutWorkingDirectory() throws MojoFailureException, MojoExecutionException
    {
        // Setting up fixture
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setSchemaBaseDirectory(schemaBaseDirectory)
            .addSchema(alphaSchema)
            .addSchema(betaSchema)
            .build();

        // Exercising system under test
        compiler.compile();
    }

    @Test(expected = MojoFailureException.class)
    public void withoutSchema() throws MojoFailureException, MojoExecutionException
    {
        // Setting up fixture
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setSchemaBaseDirectory(schemaBaseDirectory)
            .setWorkDirectory(workDirectory)
            .build();

        // Exercising system under test
        compiler.compile();
    }

    // [Utilities]

    private static DirectoryAssert assertThat(File directory)
    {
        return new DirectoryAssert(directory);
    }

    private static class DirectoryAssert
    {
        private File directory;

        public DirectoryAssert(File directory)
        {
            this.directory = directory;
        }

        public DirectoryAssert contains(String filename)
        {
            Assertions.assertThat(
                new File(directory, filename)).exists();

            return this;
        }
    }
}
