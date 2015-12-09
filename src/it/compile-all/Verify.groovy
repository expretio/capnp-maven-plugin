import static org.expretio.maven.plugins.capnp.util.TestUtils.*;

import java.io.*;

String baseDirectory = "target/it/compileAll"
String packageBase = "org/expretio/maven/plugins/capnp";

File workDirectory = new File(baseDirectory + "/work");
File outputDirectory = new File(baseDirectory + "/output");

assertThat(workDirectory)
    .contains(packageBase + "/alpha/alpha.capnp")
    .contains(packageBase + "/beta/beta.capnp")
    .contains("capnp")
    .contains("capnpc-java")
    .contains("java.capnp");

assertThat(outputDirectory)
    .contains(packageBase + "/alpha/AlphaCapnp.java")
    .contains(packageBase + "/beta/BetaCapnp.java");


