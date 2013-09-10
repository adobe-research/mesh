
// cosine similarity over vectors represented by lists of floats.
// no length agreement checking.

mag(v) { sqrt(fsum(v | fsq)) };

dot(v, w) { fsum(index(v) | { v[$0] *. w[$0] }) };

cos(v, w) { dot(v, w) /. (mag(v) *. mag(w)) };


// find max continuous subsequence of a nonempty list of ints.
// interesting cases include negative numbers in the list.
// for illustration, return value is triple of
// - input array
// - parallel array of (start index, total) for max
//   subsequence at each position
// - winning subsequence
// e.g.
// > mvcs(draw(5, 100) @- 50)     // arg is 5 random ints between -50 and 50
// ([-3, -45, 6, 37, -46], [(0, -3), (1, -45), (2, 6), (2, 43), (2, -3)], [6, 37])
// TODO decomposing params
//
mvcs(a)
{
    // compute b parallel to a, with b[i] = (start, sum) of max subsequence
    // ending at i.
    // reducer implements the decision at i to extend i-1's max subsequence,
    // or start a new one.
    // also tracks and returns position of winning subsequence.
    //
    (b, win, _) = reduce({ state, v =>
        (b, win, n) = state;
        // if max at i - 1 is > 0, extend, otherwise start new
        (start, tot) = last(b);
        info = if(tot > 0, { (start, tot + v) }, { (n, v) });
        // result
        (append(b, info),                   // append new info
         iif(info.1 > b[win].1, n, win),    // update winning position
         n + 1)                             // increment loop var
    }, ([(0, head(a))], 0, 1), tail(a));

    // return input, info array and winning subsequence
    (a, b, mapll(fromto(b[win].0, win), a))
};

// knapsack 0/1 solver--recursive w/memoization
// NOTE: a deficiency in our codegen strategy for closures prevents
// us from using the memo() library function with recursive functions:
//
//  f = memo({ ... f() ... })
//
// TODO
//
knap01a(items, wlim)
{
    comps = box(0);

    mem = box([:]);

    m(i, w) {
        if(iskey(*mem, (i, w)), { (*mem)[(i, w)] }, {
            comps <- inc;
            if(i < 0, { (items: [], w: 0, v: 0) }, {
                item = items[i];
                prev = i - 1;
                if(w < item.w, { m(prev, w) }, {
                    m1 = m(prev, w);
                    m2 = m(prev, w - item.w);
                    m2v = m2.v + item.v;
                    res = guard(m1.v >= m2v, m1, {
                        (items: m2.items + [item], w: m2.w + item.w, v: m2v)
                    });
                    mem <- { mapset($0, (i, w), res) };
                    res
                })
            })
        })
    };

    result = m(size(items) - 1, wlim);

    print("knapsack recursive + memo, wlim:", wlim, "#comps: ", *comps);    // 400, 6485

    result
};

// knapsack 0/1 solver--uses reduce to build a full table
// of solutions eagerly, then read desired one from the end.
// on the one hand, avoids repeated computations without need
// of memoization. on the other hand, will compute unneeded
// entries.
//
knap01b(items, wlim)
{
    comps = box(0);

    m = reduce({ m, i =>
        r = count(wlim + 1) | { w =>
            comps <- inc;
            item = items[i];
            if(w < item.w, { m[i][w] }, {
                m1 = m[i][w];
                m2 = m[i][w - item.w];
                m2v = m2.v + item.v;
                guard(m1.v >= m2v, m1, {
                    (items: m2.items + [item], w: m2.w + item.w, v: m2v)
                })
            })
        };
        append(m, r)
    }, [rep(wlim + 1, (items: [], w: 0, v: 0))], index(items));

    print("knapsack reduce, wlim:", wlim, "#comps:", *comps);   // 400, 8822

    m[size(items)][wlim]
};

items = [
    ("map",                     9,       150),
    ("compass",                 13,      35),
    ("water",                   153,     200),
    ("sandwich",                50,      160),
    ("glucose",                 15,      60),
    ("tin",                     68,      45),
    ("banana",                  27,      60),
    ("apple",                   39,      40),
    ("cheese",                  23,      30),
    ("beer",                    52,      10),
    ("suntancream",             11,      70),
    ("camera",                  32,      30),
    ("T-shirt",                 24,      15),
    ("trousers",                48,      10),
    ("umbrella",                73,      40),
    ("waterproof trousers",     42,      70),
    ("waterproof overclothes",  43,      75),
    ("note-case",               22,      80),
    ("sunglasses",              7,       20),
    ("towel",                   18,      12),
    ("socks",                   4,       50),
    ("book",                    30,      10)
] | { t => (name: t.0, w: t.1, v: t.2) };

// choose m items from array a, simple recursion.
// incoming m is limited to size(a)
//
choose(a, m) {
    guard(m <= 0, [[]], {
        flatten(count(max(0, size(a) - m + 1)) | { i =>
            [a[i]] +@ choose(drop(i + 1, a), m - 1);
        })
    })
};
