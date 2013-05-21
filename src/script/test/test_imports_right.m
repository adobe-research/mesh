
import b from test.test_imports_middle;
import test.test_imports_middle qualified;

type RIGHT = (Int, Int);

lambda() { true };

rightop(x) { lambda() };

get_middle_count() { test.test_imports_middle.get_count() };
