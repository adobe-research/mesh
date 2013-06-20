
// reactor provokes itself.
// currently overflows stack, need strategy

b = box(0);
done = box(false);

react(b, { v =>
    print(taskid(), v, b);
    when(v < 10000, { b <- inc });
    when(v == 10000, { done := true })
});

print(taskid());
update(b, inc);

await(done, id);
