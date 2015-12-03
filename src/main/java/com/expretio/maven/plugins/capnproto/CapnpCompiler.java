package com.expretio.maven.plugins.capnproto;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

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

    private Command command;
    private List<String> schemas;

    /**
     * Constructor.
     */
    private CapnpCompiler(File outputDir, File schemaBaseDir, List<File> importDirs, List<String> schemas)
        throws MojoExecutionException, MojoFailureException
    {
        this.command = new Command(outputDir, schemaBaseDir, importDirs);
        this.schemas = schemas;
    }

    public void compile() throws MojoExecutionException
    {
        for (String schema : schemas)
        {
            compile(schema);
        }
    }

    // [Utility methods]

    private void compile(String schema) throws MojoExecutionException
    {
        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder(command.get(schema))
                .directory(command.schemaBaseDirectory)
                .inheritIO();

            Process process = processBuilder.start();

            int exit = process.waitFor();

            if (exit != 0)
            {
                throw new MojoExecutionException("Unexpected exit value (" + exit + ") while compiling " + schema);
            }
        }
        catch(IOException | InterruptedException e)
        {
            throw new MojoExecutionException("Cannot compile capnproto schemas " + schema + ": " + e.getMessage());
        }
    }

    // [Inner classes]

    private static class Command
    {
        private ResourceProvider resources = ResourceProvider.create();

        private File outputDirectory;
        private File schemaBaseDirectory;
        private List<File> importDirectories;

        private List<String> base = Lists.newArrayList();

        public Command(File outputDir, File schemaBaseDir, List<File> importDirs)
            throws MojoExecutionException, MojoFailureException
        {
            this.outputDirectory = outputDir;
            this.schemaBaseDirectory = schemaBaseDir;
            this.importDirectories = importDirs;

            initialize();
        }

        public List<String> get(String schema)
        {
            List<String> fullCommand = Lists.newArrayList(base);
            fullCommand.add(schema);

            return fullCommand;
        }

        private void initialize() throws MojoExecutionException
        {
            outputDirectory.mkdirs();
            importDirectories.add(resources.getJavaSchema().getParentFile());
            importDirectories.add(schemaBaseDirectory);

            setBase();
        }

        private void setBase() throws MojoExecutionException
        {
            base.add(resources.getCapnp().getAbsolutePath());
            base.add("compile");
            base.add("--verbose");
            base.add("-o" + resources.getCapnpcJava().getAbsolutePath() + ":" + outputDirectory.getAbsolutePath());

            for (File importDirectory : importDirectories)
            {
                base.add("-I" + importDirectory.getAbsolutePath());
            }
        }
    }

    public static class Builder
    {
        private File outputDirectory;
        private File schemaBaseDirectory;
        private List<File> importDirectories = Lists.newArrayList();
        private List<String> schemas = Lists.newArrayList();

        public CapnpCompiler build() throws MojoExecutionException, MojoFailureException
        {
            validate();

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

        public Builder addSchema(String schema)
        {
            schemas.add(schema);

            return this;
        }

        public Builder addSchemas(Collection<String> schemas)
        {
            this.schemas.addAll(schemas);

            return this;
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
        }
    }

}
