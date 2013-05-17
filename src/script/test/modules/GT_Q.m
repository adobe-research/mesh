
import * from unittest;
import GT;

assert_equals({ GT.localfoo() }, { GT.localvar });
assert_equals({ GT.N.foo() }, { GT.N.var });
