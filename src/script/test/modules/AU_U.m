
import * from unittest;
import * from AU;

assert_equals({ localfoo() }, { localvar });
assert_equals({ foo() }, { var });

