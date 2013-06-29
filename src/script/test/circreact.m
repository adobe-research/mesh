
// reactor provokes itself.
// should not overflow stack.

b = box(0);
done = box(false);

react(b, { v =>
    when(v % 100 == 0, { print(taskid(), v, b) });
    when(v < 10000, { b <- inc });
    when(v == 10000, { done := true })
});

print(taskid());
update(b, inc);

await(done, id);
