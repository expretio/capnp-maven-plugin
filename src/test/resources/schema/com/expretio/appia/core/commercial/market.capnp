@0x9be343a3e0eb03f2;

using Java = import "/java.capnp";

$Java.package("com.expretio.appia.core.commercial");
$Java.outerClassname("MarketCapnp");

struct MarketStruct
{
  code @0 :Text;
  distance @1 :Int32;
}