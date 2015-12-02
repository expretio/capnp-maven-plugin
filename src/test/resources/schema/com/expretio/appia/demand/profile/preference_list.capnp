@0xd51005dbefb0f836;

using Java = import "/java.capnp";

using import "/com/expretio/appia/demand/alternative/alternative.capnp".AlternativeStruct;
using import "/com/expretio/appia/demand/alternative/terminal.capnp".TerminalEnum;

$Java.package("com.expretio.appia.demand.profile");
$Java.outerClassname("PreferenceListCapnp");

struct PreferenceListStruct
{
    alternatives @0 :List(AlternativeStruct);
    terminal @1 :TerminalEnum;
}