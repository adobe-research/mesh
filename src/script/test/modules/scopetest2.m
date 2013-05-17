
import * from unittest;

import * from scopeA;
import * from scopeB;
import * from scopeC;

value = "scope2";

assert_equals( { value }, { "scope2" } );
