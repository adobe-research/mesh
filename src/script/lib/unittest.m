//
// Testing functions
//

/** {@link logging.m} */
import logging;

assert_equals(expected, actual)
{
    (e, a) = (expected(), actual());
    if(e == a, {
        print(strcat(["[PASSED] ", tostr(expected), " == ", tostr(actual)]));
        true
    }, {
        logerror(strcat(["[FAILED] ", tostr(expected), " == ", tostr(actual),
            " Expected: ", tostr(e), " Actual: ", tostr(a)]));
        false
    })
};

assert_true(expr)
{
    if(expr(), {
        print(strcat(["[PASSED] ", tostr(expr), " == true"]));
        true
    }, {
        logerror(strcat(["[FAILED] ", tostr(expr), " == true"]));
        false
    })
};

assert_false(expr)
{
    if(not(expr()), {
        print(strcat(["[PASSED] ", tostr(expr), " == false"]));
        true
    }, {
        logerror(strcat(["[FAILED] ", tostr(expr), " == false"]));
        false
    })
};
