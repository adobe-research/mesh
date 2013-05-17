
import * from unittest;
import * from DT;

assert_equals({ localfoo() }, { localvar });
assert_equals({ M.foo() }, { M.var });

