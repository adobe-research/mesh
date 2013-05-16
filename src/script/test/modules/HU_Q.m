
import * from unittest;
import HU;

assert_equals({ HU.localfoo() }, { HU.localvar });
assert_equals({ HU.N.var }, { HU.N.var });
