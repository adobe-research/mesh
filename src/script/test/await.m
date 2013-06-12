
// test multiple threads waiting on a single box

test(n)
{
    print("test");

    b = box(-1);
    ntasks = box(0);

    for(count(n), { i =>
        spawn {
            ntasks <- inc;
            print("set ntasks to", *ntasks);
            await(b, { $0 == i });
            ntasks <- dec
        }
    });

    await(ntasks, { print("ntasks", $0, "..."); $0 == n });

    for(count(n), { print("b := ", $0, "..."); b := $0 });

    //timeout = box(false);
    //spawn { sleep(1000); timeout := true };

    //awaits((timeout, ntasks), { b, n => b || { n == 0 } });
    await(ntasks, { print("ntasks", $0, "..."); $0 == 0 });

    if(*ntasks == 0, { printstr(".") }, {
        print("*** DROPPED TASKS: ", *ntasks, "***")
    });

    (*ntasks == 0, *ntasks)
};

runs = repeat(100, { test(100) });

// assert(empty(filter(runs, not)));
print("total bad runs, total dropped tasks:", (size(filter(runs, fst $ not)), sum(runs | snd)));
