package org.expretio.maven.plugins.capnp;

import static org.expretio.maven.plugins.capnp.utils.ConvertUtils.*;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultMavenProjectHelper;
import org.apache.maven.project.MavenProject;
import org.expretio.maven.plugins.capnp.utils.Platform;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;


public abstract class ArtifactHandlerMojo
    extends AbstractMojo
{
    private DefaultMavenProjectHelper projectHelper = new  DefaultMavenProjectHelper();

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject mavenProject;

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepository;

    @Parameter(defaultValue = "true")
    private boolean resolveArtifact;

    protected void handleCapnpNativesDependency() throws MojoFailureException
    {
        Platform platform = Platform.detect();

        Artifact artifact = createCapnpNativesArtifact(platform);

        if (resolveArtifact)
        {
            artifact = resolve(artifact);
        }

        projectHelper.attachArtifact(mavenProject, artifact);
    }

    // [Utility methods]

    private Artifact createCapnpNativesArtifact(Platform platform)
    {
        DefaultArtifactFactory factory = new DefaultArtifactFactory();

        Artifact artifact = factory.createArtifactWithClassifier("org.expretio.maven", "capnp-natives",
            mavenProject.getVersion(), "jar", platform.getClassifier());

        return artifact;
    }

    private Artifact resolve(Artifact artifact) throws MojoFailureException
    {
        ArtifactRequest request = new ArtifactRequest(toAether(artifact), remoteRepository, null);

        try
        {
            ArtifactResult result = repositorySystem.resolveArtifact(repositorySession, request);

            return toMaven(result.getArtifact());
        }
        catch (ArtifactResolutionException e)
        {
            throw new MojoFailureException("Cannot resolve artifact: " + artifact, e);
        }
    }

}
