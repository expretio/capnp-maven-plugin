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

import static com.google.common.io.Files.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.expretio.maven.plugins.capnp.util.JavaPlatform;
import org.expretio.maven.plugins.capnp.util.NativesManager;
import org.expretio.maven.plugins.capnp.util.NativesManager.NativesInfo;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;


@Mojo(
    name = "generate",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    threadSafe = true,
    requiresProject = true,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    requiresOnline = false
)
public class CapnProtoMojo
    extends AbstractMojo
{
    private static final String automaticClassifier = "auto";
    private static final String defaultNativeDependencyVersion = "0.5.3-SNAPSHOT";

    @Component
    private RepositorySystem repositorySystem;

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    @Parameter( defaultValue = "${repositorySystemSession}", readonly = true )
    private RepositorySystemSession repositorySession;

    @Parameter( defaultValue = "${project.remoteProjectRepositories}", readonly = true )
    private List<RemoteRepository> remoteRepository;

    @Parameter( defaultValue = "true" )
    private boolean verbose;

    /**
     * Output directory of generated java classes.
     */
    @Parameter( defaultValue = "${project.build.directory}/generated-sources/capnp", required = true )
    private File outputDirectory;

    /**
     * Base directory of definition schemas.
     */
    @Parameter( defaultValue = "src/main/capnp/schema", required = true )
    private File schemaDirectory;

    /**
     * Compilation process working directory.
     */
    @Parameter( defaultValue = "${project.build.directory}/capnp-work", required = true )
    private File workDirectory;

    /**
     * File extension of definition schemas.
     */
    @Parameter( defaultValue = "capnp" )
    private String schemaFileExtension;

    /**
     * Explicitly specified definition schema files. If none, all files matching <code>schemaFileExtension<code> under
     * <code>schemaDirectory<code> will be compiled. Files must be specified relatively from
     * <code>schemaDirectory<code>.
     *
     * @see #schemaFileExtension
     * @see #schemaDirectory
     */
    @Parameter
    private String[] schemas;

    /**
     * Supplementary import directories. Note: <code>schemaDirectory</code> is implicitly considered as an import
     * directory.
     *
     * @see #schemaDirectory
     */
    @Parameter
    private File[] importDirectories;

    /**
     * Version of the <code>org.expretio.maven:capnp-natives</code> dependency.
     */
    @Parameter( defaultValue = defaultNativeDependencyVersion, required = true )
    private String nativeDependencyVersion ;

    /**
     * Classifier of the <code>org.expretio.maven:capnp-natives</code> dependency, forcing the targeted platform when
     * specified. It is recommended to use the default value, which adjusts the classifier to current platform
     * automatically.
     */
    @Parameter( defaultValue = automaticClassifier, required = true )
    private String nativeDependencyClassifier;

    /**
     * Set to false to configure manually the <code>org.expretio.maven:capnp-natives</code> dependency.
     */
    @Parameter( defaultValue = "false", required = true )
    private boolean handleNativeDependency;

    private NativesManager nativesManager;
    private NativesInfo currentNativesInfo;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( handleNativeDependency )
        {
            doHandleNativeDependency();
        }
        else
        {
            nativesManager = new NativesManager();
        }

        nativesManager.registerAllClassPathDescriptors();
        currentNativesInfo = nativesManager.getNativesInfoForCurrentPlatform();

        mavenProject.addCompileSourceRoot( outputDirectory.getAbsolutePath() );

        workDirectory.mkdirs();

        CapnpCompiler compiler =
            CapnpCompiler.builder()
                .setOutputDirectory( outputDirectory )
                .setSchemaDirectory( schemaDirectory )
                .setWorkDirectory( workDirectory )
                .setCapnpFile( copyResource( currentNativesInfo.getCapnpUrl(), workDirectory ) )
                .setCapnpcJavaFile( copyResource( currentNativesInfo.getCapnpcJavaUrl(), workDirectory ) )
                .setCapnpJavaSchemaFile( copyResource( currentNativesInfo.getCapnpJavaSchemaUrl(), workDirectory ) )
                .addSchemas( getSchemas() )
                .addImportDirectories( getImportDirectories() )
                .setVerbose( verbose )
                .build();

        compiler.compile();
    }

    private void doHandleNativeDependency()
        throws MojoExecutionException
    {
        Artifact artifact = createNativeArtifact();

        nativesManager = new NativesManager( new URLClassLoader( new URL[] { resolve( artifact ) } ) );
    }

    private Artifact createNativeArtifact()
        throws MojoExecutionException
    {
        String classifier = nativeDependencyClassifier;

        if ( classifier.equals( automaticClassifier ) )
        {
            classifier =
                (
                    JavaPlatform.getCurrentOs().name().toLowerCase()
                        + "-"
                        + JavaPlatform.getCurrentArch().toLowerCase()
                );
        }

        return new DefaultArtifact(
                    "org.expretio.maven",
                    "capnp-natives",
                    classifier,
                    "jar",
                    nativeDependencyVersion );
    }

    private URL resolve( Artifact artifact )
        throws MojoExecutionException
    {
        ArtifactRequest request = new ArtifactRequest( artifact, remoteRepository, null );

        try
        {
            return
                repositorySystem
                    .resolveArtifact( repositorySession, request )
                    .getArtifact()
                    .getFile()
                    .toURI()
                    .toURL();
        }
        catch ( ArtifactResolutionException | MalformedURLException e )
        {
            throw new MojoExecutionException( "Cannot resolve artifact: " + artifact, e );
        }
    }

    private Collection<String> getSchemas()
    {
        if ( schemas == null )
        {
            return getAllSchemas();
        }

        return Arrays.asList( schemas );
    }

    private Collection<String> getAllSchemas()
    {
        List<String> allSchemas = Lists.newArrayList();

        for ( File file : fileTreeTraverser().preOrderTraversal( schemaDirectory ) )
        {
            if ( isSchema( file ) )
            {
                allSchemas.add( relativize( file.toPath() ) );
            }
        }

        return allSchemas;
    }

    private Collection<File> getImportDirectories()
    {
        if ( importDirectories == null )
        {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList( importDirectories );
    }

    private boolean isSchema( File file )
    {
        if ( file.isDirectory() )
        {
            return false;
        }

        return file.getName().endsWith( "." + schemaFileExtension );
    }

    private String relativize( Path path )
    {
        String relativized = schemaDirectory.toPath().relativize( path ).toString();

        // capnp native program is not compatible with windows file separator
        return relativized.replace( '\\', '/' );
    }

    private File copyResource( URL source, File target )
        throws MojoExecutionException
    {
        try
        {
            String fileName = new File( source.getPath() ).getName();
            File targetFile = new File( target, fileName );

            try (
                InputStream in = new BufferedInputStream( source.openStream() );
                OutputStream out = new BufferedOutputStream( new FileOutputStream( targetFile ) );
            )
            {
                ByteStreams.copy( in, out );

                targetFile.setExecutable( true );

                return targetFile;
            }
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to copy natives to work directory: " + workDirectory, e );
        }
    }
}
