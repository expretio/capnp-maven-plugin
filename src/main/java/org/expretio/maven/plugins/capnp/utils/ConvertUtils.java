package org.expretio.maven.plugins.capnp.utils;

import org.apache.maven.artifact.Artifact;

public class ConvertUtils
{
    private ConvertUtils(){}

    public static org.eclipse.aether.artifact.Artifact toAether(Artifact artifact)
    {
        return new org.eclipse.aether.artifact.DefaultArtifact(
            artifact.getGroupId(),
            artifact.getArtifactId(),
            artifact.getClassifier(),
            artifact.getType(),
            artifact.getVersion());
    }

    public static Artifact toMaven(org.eclipse.aether.artifact.Artifact artifact)
    {
        return new org.apache.maven.artifact.DefaultArtifact(
            artifact.getGroupId(),
            artifact.getArtifactId(),
            org.apache.maven.artifact.versioning.VersionRange.createFromVersion(artifact.getVersion()),
            null,
            artifact.getExtension(),
            artifact.getClassifier(),
            new org.apache.maven.artifact.handler.DefaultArtifactHandler(),
            false);
    }
}
