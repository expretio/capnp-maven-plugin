package org.expretio.maven.plugins.capnp;

import static org.expretio.maven.plugins.capnp.utils.ConvertUtils.*;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultMavenProjectHelper;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.expretio.maven.plugins.capnp.utils.Platform;


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
        return new org.apache.maven.artifact.DefaultArtifact(
            "org.expretio.maven",
            "capnp-natives",
            org.apache.maven.artifact.versioning.VersionRange.createFromVersion("0.5.3-SNAPSHOT"),
            null,
            "jar",
            platform.getClassifier(),
            new org.apache.maven.artifact.handler.DefaultArtifactHandler(),
            false);
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
