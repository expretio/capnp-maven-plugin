package com.expretio.maven.plugins.capnproto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.common.collect.Lists;

/**
 * Implements a java adapter of capnproto compiler, creating java classes from schema definitions.
 *
 * @see #builder()
 */
public class CapnpCompiler
{
    private static final String RESOURCE_DIR = "src/main/resources/";
    private static final String CAPNP_PROGRAM = RESOURCE_DIR + "capnp";
    private static final String CAPNPC_JAVA = RESOURCE_DIR + "capnpc-java";
    private static final String JAVA_SCHEMA = RESOURCE_DIR + "java.capnp";
    private static final String DEFAULT_OUTPUT_DIR = "target/generated-sources";
    private static final String DEFAULT_SCHEMA_BASE_DIR = "src/main/schema";

    public static Builder builder()
    {
        return new Builder();
    }

    private List<File> importDirectories;
    private File outputDirectory;
    private File schemaBaseDirectory;
    private List<File> schemas;

    private File capnpcjava = new File(CAPNPC_JAVA);

    /**
     * Constructor.
     */
    private CapnpCompiler(List<File> importDirectories, File outputDirectory, File schemaBaseDirectory,
        List<File> schemas)
            throws MojoFailureException
    {
        this.importDirectories = importDirectories;
        this.outputDirectory = outputDirectory;
        this.schemaBaseDirectory = schemaBaseDirectory;
        this.schemas = schemas;

        validate(importDirectories, outputDirectory, schemaBaseDirectory, schemas);
        initialize();
    }

    public int compile() throws MojoExecutionException
    {
        List<String> command = createCommand();

        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                .directory(schemaBaseDirectory)
                .inheritIO();

            Process process = processBuilder.start();

            return process.waitFor();
        }
        catch(IOException | InterruptedException e)
        {
            throw new MojoExecutionException("Cannot compile capnproto schemas: " + e.getMessage());
        }
    }

    // [Utility methods]

    private List<String> createCommand() throws MojoExecutionException
    {
        List<String> command = Lists.newArrayList();

        command.add(getBaseCommand());
        command.add("compile");
        command.add("--verbose");
        command.add("-o" + capnpcjava.getAbsolutePath() + ":" + outputDirectory.getAbsolutePath());

        for (File importDirectory : importDirectories)
        {
            command.add("-I" + importDirectory.getAbsolutePath());
        }

        for (File schema : schemas)
        {
            command.add(schema.getPath());
        }

        // FIXME: remove
        System.out.println("command: " + command);

        return command;
    }

    private String getBaseCommand() throws MojoExecutionException
    {
        File file = createTempFile();

        try(OutputStream os = new FileOutputStream(file);
            InputStream is = new FileInputStream(CAPNP_PROGRAM);)
        {
            IOUtils.copy(is, os);
        }
        catch(IOException ioe)
        {
            throw new MojoExecutionException("Cannot copy program file:" + ioe.getMessage());
        }

        return file.getAbsolutePath();
    }

    private File createTempFile() throws MojoExecutionException
    {
        try
        {
            File file = File.createTempFile("capnp", ".bin");
            file.setExecutable(true);
            file.deleteOnExit();

            return file;
        }
        catch(IOException ioe)
        {
            throw new MojoExecutionException("Cannot create temporary file: " + ioe.getMessage());
        }
    }

    private void validate(List<File> importDirectories, File outputDirectory, File schemaBaseDirectory,
        List<File> schemas)
            throws MojoFailureException
    {
        if (importDirectories == null)
        {
            throw new MojoFailureException("Import directories may be empty but not null.");
        }

        if (outputDirectory == null)
        {
            throw new MojoFailureException("Output directory must be specified.");
        }

        if (outputDirectory.isFile())
        {
            throw new MojoFailureException("Output directory must not be a file.");
        }

        if (schemaBaseDirectory == null)
        {
            throw new MojoFailureException("Schema base directory must be specified.");
        }

        if (schemas.isEmpty())
        {
            throw new MojoFailureException("At least one schema must be specified.");
        }
    }

    private void initialize()
    {
        outputDirectory.mkdirs();
    }

    // [Inner classes]

    public static class Builder
    {
        private List<File> importDirectories = Lists.newArrayList();
        private File outputDirectory = new File(DEFAULT_OUTPUT_DIR);
        private File schemaBaseDirectory = new File(DEFAULT_SCHEMA_BASE_DIR);
        private List<File> schemas = Lists.newArrayList();

        public Builder()
        {
            importDirectories.add(new File(JAVA_SCHEMA).getParentFile());
        }

        public CapnpCompiler build() throws MojoFailureException
        {
            return new CapnpCompiler(importDirectories, outputDirectory, schemaBaseDirectory, schemas);
        }

        public Builder addImportDirectory(String importDirectory)
        {
            importDirectories.add(new File(importDirectory));

            return this;
        }

        public Builder setOutputDirectory(String outputDirectory)
        {
            this.outputDirectory = new File(outputDirectory);

            return this;
        }

        public Builder setSchemaBaseDirectory(String schemaBaseDirectory)
        {
            this.schemaBaseDirectory = new File(schemaBaseDirectory);

            return this;
        }

        public Builder addSchema(String schema)
        {
            schemas.add(new File(schema));

            return this;
        }
    }

}
