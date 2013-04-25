
import * from unittest;

import * from test.test_imports_right as right;
import leftop,status from test.test_imports_left;
import test.test_imports_right;

// This lambda should never get used
lambda() { assert(false, "should not be called") };

// Types are inherited into current namespace
foo:RIGHT = ( 4, 4 ); 

// middle is only executed once
assert_equals({test.test_imports_right.get_middle_count()}, { 1 });

// Left's symbols are in default scope
assert_true({leftop()});
assert_true({status()});

// Right's symbols are in namespace 'right'
assert_true({right.rightop(lambda)});

// Right's symbols are also in default by "import *"
assert_true({rightop(lambda)});

// Scoped and defualt symbols are the same
assert_equals({right.rightop}, {rightop});
assert_equals({test.test_imports_right.rightop}, {rightop});
assert_equals({test.test_imports_left.status}, {status});

// right should inherit and export middle's symbols
assert_equals({b}, {right.b}); 
