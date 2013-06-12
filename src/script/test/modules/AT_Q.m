
import unittest;
import AT qualified;

assert_equals({ AT.localfoo() }, { AT.localvar });
assert_equals({ AT.foo() }, { AT.var });
