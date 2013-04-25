
import * from std;
import * from unittest;

// Transaction system uses age-based arbitration when write contention
// occurs, but avoids preemption, so that a transaction is inevitable
// once all necessary resource relationships have been established.
// Arbtration avoids starvation by allowing a long-running transaction
// to queue for a resource without giving up already-acquired resources.
//
// Read-write contention: here a long-running transaction that attempts to
// *read from* a box late in its life should be able to complete with at most
// a single retry, despite a series of short-lived transactions competing for
// the same box.

b = box(0);
donelong = box(false);
doneshorts = box(false);

long() {
    do {
        print("starting long");
        sleep(1000);
        x = *b;
        print("done long");
    };
    donelong := true;
};

shorts() {
    n = box(0);
    while({!*donelong && {*n < 100}}, {
        do {
            n <- inc;
            print("starting short #", *n);
            sleep(100);
            b := 0;
            print("done short");
        }
    });
    assert_true({*n < 12});
    doneshorts := true;
};

spawn(shorts);
spawn(long);

await(doneshorts, id);
