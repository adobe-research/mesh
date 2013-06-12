
// Transaction system uses age-based arbitration when write contention
// occurs, but avoids preemption, so that a transaction is inevitable
// once all necessary resource relationships have been established.
// Arbtration avoids livelock and should ensure progress among
// contending tasks. Here two transactions which acquire common
// resources in opposite order should make roughly even progress.

a = box(0);
b = box(0);
running = box(2);

trana() {
    cycle(0, { i => get(cont) }, { i =>
        do {
            //print("a...");
            update(a, inc);
            update(b, inc);
            sleep(100);
            print("...a", taskid(), i);
            i+1
        }
    });
    update(running, dec);
};

tranb() {
    cycle(0, {i => get(cont)}, {i =>
        do {
            //print("    b...");
            update(b, inc);
            update(a, inc);
            sleep(100);
            print("    ...b", taskid(), i);
            i+1
        }
    });
    update(running, dec);
};

cont = box(true);

[trana, tranb] | spawn;

sleep(5000);
put(cont, false);
await(running, { $0 == 0 })
