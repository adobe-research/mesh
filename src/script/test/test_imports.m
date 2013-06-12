
/**

New import/export behavior:

EXPORT

Summary of changes:
- only local definitions are exportable
- export all local defs not indicated by "export *", not "export .".
  the latter is no longer legal syntax.
- default is now to export all local defs.

Details:
A module may only export local definitions--that is, definitions
created within the exporting module. (An exported definition is
one that is accessible to importing modules.) Valid forms for
the export statement are:

1. export *;
Exports all local definitions. This is the default--
if no export statement is present, all local definitions are
exported.

2. export x, y, z;
Exports local definitions x, y, z. Specifying names which do
not correspond to definitions is an error, and should be reported
by the compiler. Note that since only local definitions are exportable,
and local definitions are always unqualified (currently), these will
always be simple names.

3. export ();
Exports no definitions.

If we include the absent export statement case, this gives us 4 forms
of export to test.

--

IMPORT

Summary of changes:
- "import * from M ..." no longer valid syntax, use "import M ..." instead.
- lack of importable qualified symbols simplifies import specifications a bit.

Details:
A module may import some or all definitions from another module,
into either its local namespace or some specified nonlocal namespace.
Valid forms for the import statement are the combinations of valid
Definition and Namespace specifiers, as follows:

1. Definition specifiers:

a. no specifier, meaning import all available (that is, exported) symbols. E.g.

    import M;   // imports all available symbols from M into local namespace

b. enumerated, meaning import the specified definitions. E.g.

    import x, y, z from M;

Note: it is an error to specify definitions that are not available in the
imported module. Also note that since only local definitions are exportable,
these will always be simple, not qualified names.

c. '()', meaning import no definitions. E.g.

    import () from M;

This will ensure that M is loaded and initialized, but imports no definitions
from it.

*** Note: as before, a module should only be loaded and initialized once,
regardless of the number of times it is imported. ***

2. Namespace specifiers:

a. no specifier, meaning that definitions are imported into the importing
module's local namespace. E.g.

    import M;

b. 'qualified', meaning that definitions are imported into a namespace
named after the module being imported. E.g.

    import M qualified;

    Note that a module name may be qualified, specifying a file path. In
    such cases the unqualified (leaf) name will be used for the namespace.
    E.g.

    import x.y.M qualified;

    Here x.y.M's local definitions will be imported into the namespace M.

c. explicit namespace: here the namespace is given directly, as in

    import M into N.

**/

import unittest;

import () from modules.combotest;
import () from modules.scopetest;
import () from modules.qualifiedtypetest;

import test.test_imports_right;
import test.test_imports_right into right;
import leftop,status from test.test_imports_left;
import test.test_imports_right qualified;
import test.test_imports_left qualified;

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
