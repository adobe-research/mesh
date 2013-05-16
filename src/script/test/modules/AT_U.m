
import * from unittest;
import * from AT;

assert_equals({ localfoo() }, { localvar });
assert_equals({ foo() }, { var });
