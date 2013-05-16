
import * from unittest;
import DU;

assert_equals({ DU.localfoo() }, { DU.localvar });
assert_equals({ DU.M.foo() }, { DU.M.var });
