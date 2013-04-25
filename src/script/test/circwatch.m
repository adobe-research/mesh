
import * from std;

// watcher provokes itself.
// should run until b == 10000, not overflow stack.

b = box(0);
done = box(false);

watch(b, { o, n =>
    print(taskid(), o, n, b);
    when(n < 10000, { update(b, inc) });
    when(n == 10000, { put(done, true) })
});

print(taskid());
update(b, inc);

await(done, id);
