
import * from unittest;
import * from HT;

assert_equals({ localfoo() }, { localvar });
assert_equals({ N.foo() }, { N.var });
