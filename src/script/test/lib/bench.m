import * from std;
import * from unittest;

// bench = { b => t0 = millitime(); result = b(); (#result: result, #time: integer:l2i(lang:lminus(millitime(), t0))) }
assert_equals({ true }, {
                        res = bench({ sleep(15); inc(2) });
                        eq(3, res.result);
                        });

// benchn = { n, f => timer = { start = millitime(); f(); integer:l2i(lang:lminus(millitime(), start)) }; math:avg(list:repeat(n, timer)) }
assert_equals({ true }, {
                        res = benchn(3, { sleep(15) });
                        gt(res, 1);
                        });