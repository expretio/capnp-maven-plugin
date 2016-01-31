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
