package org.expretio.maven.plugins.capnp;

import static org.expretio.maven.plugins.capnp.util.TestUtils.*;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.expretio.maven.plugins.capnp.CapnpCompiler;
import org.junit.Test;

public class CapnpCompilerTest
{
    private String testBase = "target/compiler-test";
    private File outputDirectory = new File(testBase + "/output");
    private File workDirectory = new File(testBase + "/work");
    private File schemaDirectory = new File("src/test/resources/schema");

    private String packageBase = "org/expretio/maven/plugins/capnp";
    private String alphaSchema = packageBase + "/alpha/alpha.capnp";
    private String betaSchema = packageBase + "/beta/beta.capnp";

    @Test
    public void compile()
        throws MojoFailureException, MojoExecutionException
    {
        // Setting up fixture
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setSchemaDirectory(schemaDirectory)
            .setWorkDirectory(workDirectory)
            .addSchema(alphaSchema)
            .addSchema(betaSchema)
            .build();

        // Exercising system under test
        compiler.compile();

        // Verifying outcome
        assertThat(workDirectory)
            .contains(packageBase + "/alpha/alpha.capnp")
            .contains(packageBase + "/beta/beta.capnp")
            .contains("capnp")
            .contains("capnpc-java")
            .contains("java.capnp");

        assertThat(outputDirectory)
            .contains(packageBase + "/alpha/AlphaCapnp.java")
            .contains(packageBase + "/beta/BetaCapnp.java");
    }

    @Test(expected = MojoFailureException.class)
    public void withoutOutputDirectory()
        throws MojoFailureException, MojoExecutionException
    {
        // Setting up fixture
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setSchemaDirectory(schemaDirectory)
            .setWorkDirectory(workDirectory)
            .addSchema(alphaSchema)
            .addSchema(betaSchema)
            .build();

        // Exercising system under test
        compiler.compile();
    }

    @Test(expected = MojoFailureException.class)
    public void withoutschemaDirectory()
        throws MojoFailureException, MojoExecutionException
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
    public void withoutWorkingDirectory()
        throws MojoFailureException, MojoExecutionException
    {
        // Setting up fixture
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setSchemaDirectory(schemaDirectory)
            .addSchema(alphaSchema)
            .addSchema(betaSchema)
            .build();

        // Exercising system under test
        compiler.compile();
    }

    @Test(expected = MojoFailureException.class)
    public void withoutSchema()
        throws MojoFailureException, MojoExecutionException
    {
        // Setting up fixture
        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setSchemaDirectory(schemaDirectory)
            .setWorkDirectory(workDirectory)
            .build();

        // Exercising system under test
        compiler.compile();
    }

}
