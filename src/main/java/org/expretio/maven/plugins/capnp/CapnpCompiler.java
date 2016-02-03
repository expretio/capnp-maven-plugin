/*
 *  Copyright (c) 2015 ExPretio Technologies, Inc. and contributors
 *  Licensed under the MIT License:
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.expretio.maven.plugins.capnp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

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

    private final Command command;
    private final List<String> schemas;
    private final boolean verbose;

    /**
     * Constructor.
     */
    private CapnpCompiler( Command command, List<String> schemas, boolean verbose )
    {
        this.command = command;
        this.schemas = schemas;
        this.verbose = verbose;
    }

    public void compile()
        throws MojoExecutionException
    {
        for ( String schema : schemas )
        {
            compile( schema );
        }
    }

    // [ Utility methods ]

    private void compile( String schema )
        throws MojoExecutionException
    {
        try
        {
            ProcessBuilder processBuilder =
                    new ProcessBuilder( command.get( schema ) )
                        .directory( command.workDirectory );

            if ( verbose )
            {
                processBuilder.inheritIO();
            }

            Process process = processBuilder.start();

            int exit = process.waitFor();

            if ( exit != 0 )
            {
                throw new MojoExecutionException( "Unexpected exit value ( " + exit + " ) while compiling " + schema );
            }
        }
        catch ( IOException | InterruptedException e )
        {
            throw new MojoExecutionException( "Cannot compile schema " + schema + ".", e );
        }
    }

    // [ Inner classes ]

    private static class Command
    {
        private final File outputDirectory;
        private final File schemaDirectory;
        private final File workDirectory;
        private final File capnpFile;
        private final File capnpcJavaFile;
        private final File capnpJavaSchemaFile;
        private List<File> importDirectories;

        private List<String> base = new ArrayList<>();

        public Command(
                File outputDirectory,
                File schemaDirectory,
                File workDirectory,
                File capnpFile,
                File capnpcJavaFile,
                File capnpJavaSchemaFile,
                List<File> importDirectories )
            throws MojoExecutionException, MojoFailureException
        {
            this.outputDirectory = outputDirectory;
            this.schemaDirectory = schemaDirectory;
            this.workDirectory = workDirectory;
            this.capnpFile = capnpFile;
            this.capnpcJavaFile = capnpcJavaFile;
            this.capnpJavaSchemaFile = capnpJavaSchemaFile;
            this.importDirectories = importDirectories;

            initialize();
        }

        public List<String> get( String schema )
        {
            List<String> fullCommand = new ArrayList<>( base );
            fullCommand.add( schema );

            return fullCommand;
        }

        private void initialize()
            throws MojoExecutionException
        {
            outputDirectory.mkdirs();
            workDirectory.mkdirs();

            try
            {
                FileUtils.copyDirectoryStructure( schemaDirectory, workDirectory );

                importDirectories.add( capnpJavaSchemaFile.getParentFile() );
                importDirectories.add( schemaDirectory );

                setBase();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to initialize capnp environment.", e );
            }
        }

        private void setBase()
            throws IOException
        {
            base.add( capnpFile.getAbsolutePath() );
            base.add( "compile" );
            base.add( "--verbose" );
            base.add( "-o" + capnpcJavaFile.getAbsolutePath() + ":" + outputDirectory.getAbsolutePath() );

            for ( File importDirectory : importDirectories )
            {
                base.add( "-I" + importDirectory.getAbsolutePath() );
            }
        }
    }

    public static class Builder
    {
        private File outputDirectory;
        private File schemaDirectory;
        private File workDirectory;
        private File capnpFile;
        private File capnpcJavaFile;
        private File capnpJavaSchemaFile;
        private final List<File> importDirectories = new ArrayList<>();
        private final List<String> schemas = new ArrayList<>();
        private boolean verbose = true;

        public CapnpCompiler build()
            throws MojoExecutionException, MojoFailureException
        {
            validate();

            Command command =
                new Command(
                        outputDirectory,
                        schemaDirectory,
                        workDirectory,
                        capnpFile,
                        capnpcJavaFile,
                        capnpJavaSchemaFile,
                        importDirectories );

            return new CapnpCompiler( command, schemas, verbose );
        }

        public Builder setOutputDirectory( File outputDirectory )
        {
            this.outputDirectory = outputDirectory;

            return this;
        }

        public Builder setSchemaDirectory( File schemaDirectory )
        {
            this.schemaDirectory = schemaDirectory;

            return this;
        }

        public Builder setWorkDirectory( File workDirectory )
        {
            this.workDirectory = workDirectory;

            return this;
        }

        public Builder setCapnpFile( File capnpFile )
        {
            this.capnpFile = capnpFile;

            return this;
        }

        public Builder setCapnpcJavaFile( File capnpcJavaFile )
        {
            this.capnpcJavaFile = capnpcJavaFile;

            return this;
        }

        public Builder setCapnpJavaSchemaFile( File capnpJavaSchemaFile )
        {
            this.capnpJavaSchemaFile = capnpJavaSchemaFile;

            return this;
        }

        public Builder addImportDirectory( File importDirectory )
        {
            importDirectories.add( importDirectory );

            return this;
        }

        public Builder addImportDirectories( Collection<File> importDirectories )
        {
            this.importDirectories.addAll( importDirectories );

            return this;
        }

        public Builder addSchema( String schema )
        {
            schemas.add( schema );

            return this;
        }

        public Builder addSchemas( Collection<String> schemas )
        {
            this.schemas.addAll( schemas );

            return this;
        }

        public Builder setVerbose( boolean value )
        {
            this.verbose = value;

            return this;
        }

        private void validate()
            throws MojoFailureException
        {
            validate( outputDirectory, "Output directory" );
            validate( schemaDirectory, "Schema base directory" );
            validate( workDirectory, "Working directory" );

            validate( capnpFile, "capnpn file" );
            validate( capnpcJavaFile, "capnpnc java file" );
            validate( capnpJavaSchemaFile, "capnpn java schema file" );

            for ( File importDirectory : importDirectories )
            {
                validate( importDirectory, "Import directory" );
            }

            if ( schemas.isEmpty() )
            {
                throw new MojoFailureException( "At least one schema file must be specified." );
            }
        }

        private void validate( File file, String name )
            throws MojoFailureException
        {
            if ( file == null )
            {
                throw new MojoFailureException( name + " is mandatory." );
            }
        }
    }
}

