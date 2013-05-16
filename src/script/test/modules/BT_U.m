
import * from unittest;
import BT;

assert_equals({ BT.localfoo() }, { BT.localvar });
assert_equals({ BT.var }, { BT.var });
