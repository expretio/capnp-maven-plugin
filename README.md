[![Build Status](https://travis-ci.org/expretio/capnp-maven-plugin.svg?branch=master)](https://travis-ci.org/expretio/capnp-maven-plugin)

capnp-maven-plugin
==================

### Description

The Cap'n Proto maven plugin provides dynamic compilation of capnproto's definition schemas at build time. Generated java classes are automatically added to project source.

### Usage
---------

The simplest configuration will compile all schemas definition files in default schema directory.

```xml
<plugin>
    <groupId>org.expretio.maven.plugins</groupId>
    <artifactId>capnp-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Goal `generate`
---------------

### Attributes

* Requires a Maven project to be executed.
* The goal is thread-safe and supports parallel builds.
* Binds by default to the lifecycle phase: generate-sources.

### Configuration

| name | type | Since | Description |
| ---- | ---- | ----- | ----------- |
| outputDirectory | File | 0.5.3 | Output directory of generated java classes.<br/>**Default:** `${project.build.directory}/generated-sources/capnp` |
| schemaDirectory | File | 0.5.3 | Base directory of definition schemas.<br/>**Default:** `src/main/capnp/schema`|
| workDirectory | File | 0.5.3 | Compilation process working directory.<br/>**Default:** `${project.build.directory}/capnp-work` |
| schemaFileExtension | String | 0.5.3 | Extension of definition schema files.<br/>**Default:** `capnp`<br/>**Example:** `foo.capnp` |
| schemas | File[] | 0.5.3 | Explicitly specified definition schema files. If none, all files matching `schemaFileExtension` under `schemaDirectory` will be compiled. Files must be specified relatively from `schemaDirectory`.|
| importDirectories | File[] | 0.5.3 | Supplementary import directories. Note: `schemaDirectory` is implicitly considered as an import directory.. |
| verbose | Boolean | 0.5.3 | Set to `false` for no output.<br/>**Default:** `true` |


Dependencies
------------

todo

Example - Compile selected schemas
----------------------------------

Use `schemas` to explicity specify which schema to be compiled.

```xml
<plugin>
    <groupId>org.expretio.maven.plugins</groupId>
    <artifactId>capnp-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <schemas>
                    <schema>org/expretio/maven/plugins/capnp/alpha/alpha.capnp</schema>
                </schemas>
            </configuration>
        </execution>
    </executions>
</plugin>
```
