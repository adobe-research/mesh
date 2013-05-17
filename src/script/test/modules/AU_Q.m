
import * from unittest;
import AU;

assert_equals({ AU.localfoo() }, { AU.localvar });
assert_equals({ AU.foo() }, { AU.var });
