
import unittest;
import AT;

assert_equals({ localfoo() }, { localvar });
assert_equals({ foo() }, { var });
