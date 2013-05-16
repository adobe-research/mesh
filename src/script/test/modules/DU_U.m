
import * from unittest;
import * from DU;

assert_equals({ localfoo() }, { localvar });
assert_equals({ M.foo() }, { M.var });
