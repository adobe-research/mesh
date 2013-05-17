
import * from unittest;
import HT;

assert_equals({ HT.localfoo() }, { HT.localvar });
assert_equals({ HT.N.foo() }, { HT.N.var });
