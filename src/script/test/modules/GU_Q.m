
import * from unittest;
import GU;

assert_equals({ GU.localfoo() }, { GU.localvar });
assert_equals({ GU.N.foo() }, { GU.N.var });
