@0x9d07740d517b4e96;

using Java = import "/java.capnp";

$Java.package("com.expretio.appia.demand");
$Java.outerClassname("PeriodCapnp");

struct PeriodStruct
{
  startDbd @0 :Int32;
  endDbd @1 :Int32;
}