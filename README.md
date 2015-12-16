[![Build Status](https://travis-ci.org/expretio/capnp-maven-plugin.svg?branch=master)](https://travis-ci.org/expretio/capnp-maven-plugin)

capnp-maven-plugin
==================

### Description

The [Cap'n Proto](http://capnproto.org) maven plugin provides dynamic compilation of capnproto's definition schemas at build time. Generated java classes are automatically added to project source.

### Usage
---------

The simplest configuration will compile all schema definition files contained in default schema directory.

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
| outputDirectory | File | 1.0 | Output directory of generated java classes.<br/>**Default:** `${project.build.directory}/generated-sources/capnp` |
| schemaDirectory | File | 1.0 | Base directory of definition schemas.<br/>**Default:** `src/main/capnp/schema`|
| workDirectory | File | 1.0 | Compilation process working directory.<br/>**Default:** `${project.build.directory}/capnp-work` |
| schemaFileExtension | String | 1.0 | Extension of definition schema files.<br/>**Default:** `capnp`<br/>**Example:** `foo.capnp` |
| schemas | File[] | 1.0 | Explicitly specified definition schema files. If none, all files matching `schemaFileExtension` under `schemaDirectory` will be compiled. Files must be specified relatively from `schemaDirectory`.|
| importDirectories | File[] | 1.0 | Supplementary import directories. Note: `schemaDirectory` is implicitly considered as an import directory.. |
| verbose | Boolean | 1.0 | Set to `false` for no output.<br/>**Default:** `true` |


Dependencies
------------

todo

Example - Compiling selected schemas
------------------------------------

Use `schemas` to explicitly specify which schemas to be compiled.

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
                    <schema>org/expretio/one/foo.capnp</schema>
                    <schema>org/expretio/two/bar.capnp</schema>
                </schemas>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Example - Using `java.capnp`
----------------------------

The [java.capnp](https://dwrensha.github.io/capnproto-java/index.html) schema, providing `package` and `outerClassname` annotations, is available at the root of working directory.
The following schema file `foo.capnp` illustrates how to import it.

```java
@0xe9e172ef0f0049f6;

using Java = import "/java.capnp";

$Java.package("org.expretio.one");
$Java.outerClassname("Foo");

struct FooStruct
{
    code @0 :Text;
}
```

Example - Interdependent schemas
---------------------------------

Suppose schema `bar.capnp` depends on `FooStruct` defined in `foo.capnp`.

#####file: `${schemaDirectory}/com/expretio/one/foo.capnp`
```java
@0xe9e172ef0f0049f6;

using Java = import "/java.capnp";

$Java.package("org.expretio.one");
$Java.outerClassname("Foo");

struct FooStruct
{
    code @0 :Text;
}
```

#####file: `${schemaDirectory}/com/expretio/two/bar.capnp`
```java
@0xb5724e25782451a6;

using Java = import "/java.capnp";

using import "/org/expretio/one/foo.capnp".FooStruct;

$Java.package("org.expretio.two");
$Java.outerClassname("Bar");

struct BarStruct
{
    foo @0 :FooStruct;
}
```