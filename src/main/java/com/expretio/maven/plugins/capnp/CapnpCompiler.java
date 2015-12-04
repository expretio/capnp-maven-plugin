package com.expretio.maven.plugins.capnp;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
    private boolean verbose;

    /**
     * Constructor.
     */
    private CapnpCompiler(Command command, List<String> schemas, boolean verbose)
    {
        this.command = command;
        this.schemas = schemas;
        this.verbose = verbose;
    }

    public void compile()
        throws MojoExecutionException
    {
        for (String schema : schemas)
        {
            compile(schema);
        }
    }

    // [Utility methods]

    private void compile(String schema)
        throws MojoExecutionException
    {
        try
        {
            ProcessBuilder processBuilder =
                    new ProcessBuilder(command.get(schema))
                        .directory(command.workDirectory);

            if (verbose)
            {
                processBuilder.inheritIO();
            }

            Process process = processBuilder.start();

            int exit = process.waitFor();

            if (exit != 0)
            {
                throw new MojoExecutionException("Unexpected exit value (" + exit + ") while compiling " + schema);
            }
        }
        catch (IOException | InterruptedException e)
        {
            throw new MojoExecutionException("Cannot compile schema " + schema + ": " + e.getMessage());
        }
    }

    // [Inner classes]

    private static class Command
    {
        private ResourceProvider resources;

        private File outputDirectory;
        private File schemaBaseDirectory;
        private File workDirectory;
        private List<File> importDirectories;

        private List<String> base = Lists.newArrayList();

        public Command(
                File outputDirectory,
                File schemaBaseDirectory,
                File workDirectory,
                List<File> importDirectories)
            throws MojoExecutionException, MojoFailureException
        {
            this.resources = ResourceProvider.create(workDirectory);
            this.outputDirectory = outputDirectory;
            this.schemaBaseDirectory = schemaBaseDirectory;
            this.workDirectory = workDirectory;
            this.importDirectories = importDirectories;

            initialize();
        }

        public List<String> get(String schema)
        {
            List<String> fullCommand = Lists.newArrayList(base);
            fullCommand.add(schema);

            return fullCommand;
        }

        private void initialize()
            throws MojoExecutionException
        {
            outputDirectory.mkdirs();

            try
            {
                copySources();

                importDirectories.add(resources.getJavaSchema().getParentFile());
                importDirectories.add(schemaBaseDirectory);

                setBase();
            }
            catch (Exception e)
            {
                throw new MojoExecutionException("Unable to initialize capnp environment.", e);
            }

        }

        private void copySources()
            throws IOException
        {
            FileUtils.copyDirectory(schemaBaseDirectory, workDirectory);
        }

        private void setBase()
            throws IOException
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
        private File workDirectory;
        private final List<File> importDirectories = Lists.newArrayList();
        private final List<String> schemas = Lists.newArrayList();
        private boolean verbose = true;

        public CapnpCompiler build()
            throws MojoExecutionException, MojoFailureException
        {
            validate();

            Command command = new Command(outputDirectory, schemaBaseDirectory, workDirectory, importDirectories);

            return new CapnpCompiler(command, schemas, verbose);
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

        public Builder setWorkDirectory(File workDirectory)
        {
            this.workDirectory = workDirectory;

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

        public Builder setVerbose(boolean value)
        {
            this.verbose = value;

            return this;
        }

        private void validate()
            throws MojoFailureException
        {
            validate(outputDirectory, "Output directory");
            validate(schemaBaseDirectory, "Schema base directory");
            validate(workDirectory, "Working directory");

            for (File importDirectory : importDirectories)
            {
                validate(importDirectory, "Import directory");
            }

            if (schemas.isEmpty())
            {
                throw new MojoFailureException("At least one schema file must be specified.");
            }
        }

        private void validate(File directory, String name)
            throws MojoFailureException
        {
            if (directory == null)
            {
                throw new MojoFailureException(name + " must be specified.");
            }

            if (directory.isFile())
            {
                throw new MojoFailureException(name + " must not be a file.");
            }
        }
    }
}
