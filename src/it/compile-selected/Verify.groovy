import static org.expretio.maven.plugins.capnp.util.TestUtils.*;

import java.io.*;

import org.expretio.maven.plugins.capnp.platform.Platform;

Platform platform = Platform.detect();

String packageBase = "org/expretio/maven/plugins/capnp";

File baseDirectory = new File(basedir, "target");
File workDirectory = new File(baseDirectory, "work");
File outputDirectory = new File(baseDirectory, "output");

assertThat(workDirectory)
    .contains(packageBase + "/alpha/alpha.capnp")
    .contains(packageBase + "/beta/beta.capnp")
    .contains(platform.getCapnp())
    .contains(platform.getCapnpcJava())
    .contains(platform.getJavaSchema());

assertThat(outputDirectory)
    .contains(packageBase + "/alpha/AlphaCapnp.java")
    .doesNotContain(packageBase + "/beta/BetaCapnp.java");

return true;
