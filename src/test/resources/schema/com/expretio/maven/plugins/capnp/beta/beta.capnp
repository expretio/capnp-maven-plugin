@0xd51005dbefb0f836;

using Java = import "/java.capnp";

using import "/com/expretio/maven/plugins/capnp/alpha/alpha.capnp".AlphaStruct;

$Java.package("com.expretio.maven.plugins.capnp.beta");
$Java.outerClassname("BetaCapnp");

struct BetaStruct
{
    code @0 :Text;
    alpha @1 :AlphaStruct;
}