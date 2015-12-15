package org.expretio.maven.plugins.capnp.util;

import java.io.File;

import org.assertj.core.api.Assertions;

public final class TestUtils
{
    private TestUtils() {}

    public static DirectoryAssert assertThat(File directory)
    {
        return new DirectoryAssert(directory);
    }

    // [Inner classes]

    public static class DirectoryAssert
    {
        private File directory;

        public DirectoryAssert(File directory)
        {
            this.directory = directory;
        }

        public DirectoryAssert contains(String filename)
        {
            File file = new File(directory, filename);

            Assertions.assertThat(file).exists();
            Assertions.assertThat(file).isFile();
            Assertions.assertThat(file.length()).isPositive();

            return this;
        }

        public DirectoryAssert doesNotContain(String filename)
        {
            File file = new File(directory, filename);

            Assertions.assertThat(file).doesNotExist();

            return this;
        }

    }
}
