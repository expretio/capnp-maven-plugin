/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expretio.maven.plugins.capnproto;

import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.io.filefilter.FileFilterUtils.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.google.common.collect.Lists;

@Mojo(
        name = "generate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        threadSafe = true,
        requiresProject = true,
        requiresOnline = false
)
public class CapnProtoMojo
    extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "false")
    private boolean verbose;

    /**
     * Output directory of generated java classes.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/capnp", required = true)
    private File outputDirectory;

    /**
     * Base directory of definition schemas.
     */
    @Parameter(defaultValue = "src/main/capnp/schema", required = true)
    private File schemaBaseDirectory;

    /**
     * Compilation process working directory.
     */
    @Parameter(defaultValue = "${project.build.directory}/capnp-work", required = true)
    private File workDirectory;

    /**
     * File extension of definition schemas.
     */
    @Parameter(defaultValue = "capnp")
    private String schemaFileExtension;

    /**
     * Explicitly specified definition schema files. If none, all files matching <code>schemaFileExtension<code> under
     * <code>schemaBaseDirectory<code> will be compiled. Files must be specified relatively from
     * <code>schemaBaseDirectory<code>.
     *
     * @see #schemaFileExtension
     * @see #schemaBaseDirectory
     */
    @Parameter()
    private String[] schemas;

// FIXME:
//    @Parameter
//    private File importDirectory;
//
//    @Parameter
//    private List importDirectories;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        mavenProject.addCompileSourceRoot(outputDirectory.getAbsolutePath());

        CapnpCompiler compiler = CapnpCompiler.builder()
            .setOutputDirectory(outputDirectory)
            .setSchemaBaseDirectory(schemaBaseDirectory)
            .setWorkDirectory(workDirectory)
            .addSchemas(getSchemas())
            .setVerbose(verbose)
            .build();

        compiler.compile();
    }

    // [Utility methods]

    private Collection<String> getSchemas()
    {
        if (schemas == null)
        {
            return getAllSchemas();
        }

        return Arrays.asList(schemas);
    }

    private Collection<String> getAllSchemas()
    {
        IOFileFilter filter =
            and(
                suffixFileFilter("." + schemaFileExtension),
                fileFileFilter()
            );

        Collection<File> files = listFiles(schemaBaseDirectory, filter, TrueFileFilter.INSTANCE);

        return relativize(files);
    }

    private Collection<String> relativize(Collection<File> files)
    {
        List<String> paths = Lists.newArrayList();

        for (File file : files)
        {
            paths.add(
                schemaBaseDirectory.toPath().relativize(file.toPath()).toString()
            );
        }

        return paths;
    }
}
