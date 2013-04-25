
import * from std;

// When a synchronous child task (e.g. a task that runs as part of a
// parallelized operation, e.g. pmap) is
// (a) run from within a transaction,
// (b) creates its own transaction, and
// (c) the two transactions contend for resources, livelock can arise
// within the current system.
//
// This is because the two transactions currently run as peers, so the
// child task's transaction can never acquire ownership of the resource
// (since the parent task will never release it during the child task's
// lifetime). Below is the simple transaction shape which produces this
// situation.
//
// Adding support for this idiom will require runtime infrastructure for
// true nested transactions, or rather true nested tasks, with accompanying
// transactional capability. (Though note that the idiom itself is not that
// common--it's usually more natural to define parallel operations to return
// results functionally.)
// TODO the above

// This test currently provokes a fatal error for launching a task
// from within a transaction.
test() {
    b = box(0);

    shape() {
        starttime = millitime();
        do {
            print("start T1");
            x = get(b);                 // b is now owned by parent task
            pfor(count(1), { _ =>
                do {
                    elapsed = l2i(lminus(millitime(), starttime));
                    if (elapsed > 3000, {
                        print("giving up...");
                    }, {
                        print("start T2");
                        put(b, 1);          // fail, retry
                        print("end T2");
                    });
                }
            });
            print("end T1");
        }
    };

    shape();
    assert(get(b) == 1, "Inner transaction did not complete");
};

test();
