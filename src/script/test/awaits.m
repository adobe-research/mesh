
import * from std;
import * from unittest;

// task multi-wait

(a, b) = (box(0), box(0));

done = box(false);
nof_threads = 4;

running = box(nof_threads);

ctask() {
    spawn {
        print(taskid(), "started");
        while({!get(done)}, {
            sleep(rand(100));

            // wait until done or some box is > 0
            awaits((done, a, b), { b, i, j => b || { i + j > 0 } });

            // choose which box to consume from
            choice = rand(100) > 50;

            // respond
            (*done &&
                { print(taskid(), "done"); true}) ||
            { !choice &&
                { tau(a, {$0 > 0}, dec).0 } &&
                { print(taskid(), "consumed a, boxes:", a, b); true } } ||
            { choice &&
                { tau(b, {$0 > 0}, dec).0 } &&
                { print(taskid(), "consumed b, boxes:", a, b); true } } ||
            { print(taskid(), "already consumed, boxes:", a, b);
              false
            }
        });
        update(running, dec);
    }
};

// spawn some consumer tasks
repeat(nof_threads, ctask);

// dump some values into both boxes. should see "consumed" messasge for each value
puts((a, b), (10, 10));
print("produced");

// wait a while, then kill tasks
sleep(1000);
put(done, true);
await(running, { $0 == 0 });

print("should be 0:", *a + *b);
assert_equals({0}, {*a + *b});
