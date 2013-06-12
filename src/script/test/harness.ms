$load test.apply
$clear

$load test.await
$clear
$load test.awaits
$clear

$load test.cbuf
$clear

// TODO uncomment or remove permanently
//$load test.circwatch
//$clear

$load test.decompa
$clear
$load test.inlineparams
$clear

// Library testing
$load test.lib.bench
$clear
$load test.lib.concutil
$clear
$load test.lib.inliners
$clear
$load test.lib.test_lang
$clear
$load test.lib.logging
$clear
$load test.lib.sort
$clear

$load test.livelock
$clear
$load test.msort
$clear

// TODO test currently causes a fatal error (by design)
// $load test/nesting
// $clear

$load test.newtype
$clear
$load test.pc
$clear
$load test.rle
$clear
$load test.qsort
$clear
$load test.regress
$clear
$load test.starvation
$clear
$load test.starvation2
$clear
$load test.tagpair
$clear
$load test.tranfuncs
$clear
$load test.transtress
$clear
$load test.type_anno
$clear

// TODO rework import tests per new behavior, see test/test_imports.m for spec
// $load test.test_imports
// $clear

$load test.watches
$quit
