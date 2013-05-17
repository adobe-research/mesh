
import * from unittest;
import * from GU;

assert_equals({ localfoo() }, { localvar });
assert_equals({ N.foo() }, { N.var });
