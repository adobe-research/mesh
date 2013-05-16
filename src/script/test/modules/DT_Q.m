
import * from unittest;
import DT;

assert_equals({ DT.localfoo() }, { DT.localvar });
assert_equals({ DT.M.foo() }, { DT.M.var });

