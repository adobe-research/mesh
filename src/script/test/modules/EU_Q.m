
import * from unittest;
import EU;

assert_equals({ EU.localfoo() }, { EU.localvar });
assert_equals({ EU.M.var }, { EU.M.var });
