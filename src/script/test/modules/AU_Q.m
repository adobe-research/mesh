
import unittest;
import AU qualified;

assert_equals({ AU.localfoo() }, { AU.localvar });
assert_equals({ AU.foo() }, { AU.var });
