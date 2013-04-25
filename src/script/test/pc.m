
import * from std;

// test produce/consume library funcs

NCONS = 2;
QSIZE = 10;
NSECS = 1;

// work queue
q = box([]);

// print queue to console whenever updated
qw = react(q, {l => print("q", l)});

// producer function
prod() {
    n = rand(10);
    print("PROD", n);
n };

// consumer function
cons(n) {
    print(taskid(), "CONS", n);
    sleep(rand(10))
};

// kill switches
pc = box(true);
cc = box(true);

// spawn a single producer task
spawn { produce(pc, q, QSIZE, prod) };

// spawn NCONS consumer tasks
repeat(NCONS, { spawn { consume(cc, q, cons) } });

// let tasks run a while
sleep(NSECS * 1000);

// first kill the producer
pc := false;

// wait until the queue has been emptied, then kill consumers
await(q, empty);
cc := false;

// unwatch the queue
unwatch(q, qw);
