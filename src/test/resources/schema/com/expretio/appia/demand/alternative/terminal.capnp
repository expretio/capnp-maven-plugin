@0xabfb961f0c25784d;

using Java = import "/java.capnp";

$Java.package("com.expretio.appia.demand.alternative");
$Java.outerClassname("TerminalCapnp");

enum TerminalEnum
{
  nullChoice @0;
  prevItin @1;
  nextItin @2;
}

