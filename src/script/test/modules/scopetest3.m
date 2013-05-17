
import * from unittest;

import * from scopeA;
import scopeB;
import scopeC;

assert_equals( { value }, { "scopeA" } );
assert_equals( { scopeC.value }, { "scopeC" } );

