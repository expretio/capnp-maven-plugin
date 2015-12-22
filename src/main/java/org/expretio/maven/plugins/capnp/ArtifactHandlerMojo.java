package org.expretio.maven.plugins.capnp;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.expretio.maven.plugins.capnp.platform.Platform;


public abstract class ArtifactHandlerMojo
    extends AbstractMojo
{
    private static final String auto = "auto";

    @Component
    private RepositorySystem repositorySystem;

    @Parameter( defaultValue = "${repositorySystemSession}", readonly = true )
    private RepositorySystemSession repositorySession;

    @Parameter( defaultValue = "${project.remoteProjectRepositories}", readonly = true )
    private List<RemoteRepository> remoteRepository;

    /**
     * Version of the <code>org.expretio.maven:capnp-natives</code> dependency.
     */
    @Parameter( defaultValue = "0.5.3-SNAPSHOT", required = true )
    private String nativeDependencyVersion ;

    /**
     * Classifier of the <code>org.expretio.maven:capnp-natives</code> dependency, forcing the targeted platform when
     * specified. It is recommended to use the default value, which adjusts the classifier to current platform
     * automatically.
     */
    @Parameter( defaultValue = auto, required = true )
    private String nativeDependencyClassifier;

    /**
     * Set to false to configure manually the <code>org.expretio.maven:capnp-natives</code> dependency.
     */
    @Parameter( defaultValue = "true", required = true )
    private boolean handleNativeDependency;

    protected void doHandleNativeDependency() throws MojoFailureException
    {
        if ( !handleNativeDependency )
        {
            return;
        }

        Artifact artifact = createNativeArtifact();

        URL[] url = resolve( artifact );

        URLClassLoader urlClassLoader = new URLClassLoader( url );

        Thread.currentThread().setContextClassLoader( urlClassLoader );
    }

    // [ Utility methods ]

    private Artifact createNativeArtifact()
    {
        if ( nativeDependencyClassifier.equals( auto ) )
        {
            Platform platform = Platform.detect();
            nativeDependencyClassifier = platform.getClassifier();
        }

        return new org.eclipse.aether.artifact.DefaultArtifact(
            "org.expretio.maven",
            "capnp-natives",
            nativeDependencyClassifier,
            "jar",
            nativeDependencyVersion );
    }

    private URL[] resolve( Artifact artifact ) throws MojoFailureException
    {
        ArtifactRequest request = new ArtifactRequest( artifact, remoteRepository, null );

        try
        {
            ArtifactResult result = repositorySystem.resolveArtifact( repositorySession, request );

            return toURL( result.getArtifact() );
        }
        catch ( ArtifactResolutionException | MalformedURLException e )
        {
            throw new MojoFailureException( "Cannot resolve artifact: " + artifact, e );
        }
    }

    private URL[] toURL( Artifact artifact ) throws MalformedURLException
    {
        URL[] urls = new URL[1];
        urls[0] = artifact.getFile().toURI().toURL();

        return urls;
    }

}
