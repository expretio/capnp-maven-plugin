package com.expretio.maven.plugins.capnproto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
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
    public static Builder builder()
    {
        return new Builder();
    }

    private ResourceProvider resources = ResourceProvider.create();

    private File outputDirectory;
    private File schemaBaseDirectory;
    private List<File> importDirectories;
    private List<File> schemas;

    /**
     * Constructor.
     */
    private CapnpCompiler(File outputDir, File schemaBaseDir, List<File> importDirs, List<File> schemas)
        throws MojoFailureException
    {
        this.outputDirectory = outputDir;
        this.schemaBaseDirectory = schemaBaseDir;
        this.importDirectories = importDirs;
        this.schemas = schemas;

        validate();
        initialize();
    }

    public void compile() throws MojoExecutionException
    {
        List<String> command = createCommand();

        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                .directory(schemaBaseDirectory)
                .inheritIO();

            Process process = processBuilder.start();

            int exit = process.waitFor();

            if (exit != 0)
            {
                throw new MojoExecutionException("Unexpected compilation process exit value: " + exit);
            }
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
        command.add("-o" + resources.getCapnpcJava().getAbsolutePath() + ":" + outputDirectory.getAbsolutePath());

        for (File importDirectory : importDirectories)
        {
            command.add("-I" + importDirectory.getAbsolutePath());
        }

        for (File schema : schemas)
        {
            command.add(schema.getPath());
        }

        return command;
    }

    private String getBaseCommand() throws MojoExecutionException
    {
        File file = createTempFile();

        try(OutputStream os = new FileOutputStream(file);
            InputStream is = new FileInputStream(resources.getCapnp());)
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

    private void validate()
        throws MojoFailureException
    {
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

        if (schemaBaseDirectory.isFile())
        {
            throw new MojoFailureException("Schema base directory must not be a file.");
        }

        for (File importDirectory : importDirectories)
        {
            if (importDirectory.isFile())
            {
                throw new MojoFailureException("Import directory must not be a file: " + importDirectory);
            }
        }

        if (schemas.isEmpty())
        {
            throw new MojoFailureException("At least one schema file must be specified.");
        }

        for (File schema : schemas)
        {
            if (schema.isDirectory())
            {
                throw new MojoFailureException("Schema file must not be a directory: " + schema);
            }
        }
    }

    private void initialize()
    {
        outputDirectory.mkdirs();
        importDirectories.add(resources.getJavaSchemaDirectory());
    }

    // [Inner classes]

    public static class Builder
    {
        private File outputDirectory;
        private File schemaBaseDirectory;
        private List<File> importDirectories = Lists.newArrayList();
        private List<File> schemas = Lists.newArrayList();

        public CapnpCompiler build() throws MojoFailureException
        {
            return new CapnpCompiler(outputDirectory, schemaBaseDirectory, importDirectories, schemas);
        }

        public Builder setOutputDirectory(File outputDirectory)
        {
            this.outputDirectory = outputDirectory;

            return this;
        }

        public Builder setSchemaBaseDirectory(File schemaBaseDirectory)
        {
            this.schemaBaseDirectory = schemaBaseDirectory;

            return this;
        }

        public Builder addImportDirectory(File importDirectory)
        {
            importDirectories.add(importDirectory);

            return this;
        }

        public Builder addImportDirectory(Collection<File> importDirectories)
        {
            importDirectories.addAll(importDirectories);

            return this;
        }

        public Builder addSchema(File schema)
        {
            schemas.add(schema);

            return this;
        }

        public Builder addSchemas(Collection<File> schemas)
        {
            schemas.addAll(schemas);

            return this;
        }
    }

}
