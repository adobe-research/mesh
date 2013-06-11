
import lib.logging;
import unittest;


//////////////////////////////////////////////////
// logging
//////////////////////////////////////////////////
// logdebug : (T => T -> ()) = <intrinsic>
assert_true({ logdebug("debug message"); true });
assert_true({ apply(logdebug, ("debug message")); true });

// logerror : (T => T -> ()) = <intrinsic>
// do not test logerror, since this will cause the "test" target to fail
// since it will record the error and then exit with a non-zero exitcode

// loginfo : (T => T -> ()) = <intrinsic>
assert_true({ loginfo("info message"); true });
assert_true({ apply(loginfo, ("info message")); true });

// logwarning : (T => T -> ()) = <intrinsic>
assert_true({ logwarning("warning message"); true });
assert_true({ apply(logwarning, ("warning message")); true });
