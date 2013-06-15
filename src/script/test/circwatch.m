
// watcher provokes itself.
// should run until b == 10000, not overflow stack.

b = box(0);
done = box(false);

react(b, { v =>
    print(taskid(), v, b);
    when(n < 10000, { update(b, inc) });
    when(n == 10000, { put(done, true) })
});

print(taskid());
update(b, inc);

await(done, id);
