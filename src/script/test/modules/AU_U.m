
import unittest;
import AU;

assert_equals({ localfoo() }, { localvar });
assert_equals({ foo() }, { var });

