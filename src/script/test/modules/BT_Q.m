
import * from unittest;
import BT qualified;

assert_equals({ BT.localfoo() }, { BT.localvar });
assert_equals({ BT.var }, { BT.var });
