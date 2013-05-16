
import * from unittest;
import AT;

assert_equals({ AT.localfoo() }, { AT.localvar });
assert_equals({ AT.foo() }, { AT.var });
