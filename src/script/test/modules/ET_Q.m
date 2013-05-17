
import * from unittest;
import ET;

assert_equals({ ET.localfoo() }, { ET.localvar });
assert_equals({ ET.M.var }, { ET.M.var });

