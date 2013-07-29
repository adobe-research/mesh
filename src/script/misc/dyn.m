
// find max continuous subsequence of a nonempty list of ints.
// interesting cases include negative numbers in the list.
// for illustration, return value is triple of
// - input array
// - parallel array of (start index, total) for max
//   subsequence at each position
// - winning subsequence
// e.g.
// > mvcs(draw(5, 100) @- 50)
// ([-3, -45, 6, 37, -46], [(0, -3), (1, -45), (2, 6), (2, 43), (2, -3)], [6, 37])
// TODO this reduce is very common use-case for decomposing params
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
// issues: if/elses are too cumbersome
// box ops are too cumbersome
// iskey is necessitated by lack of variants in lookup API
// TODO log BUG on recursive closure CG (seen if memoize call is inlined)
knap01(items, wlim)
{
    m(i, w) {
        if(i < 0, { (items: [], w: 0, v: 0) }, {
            item = items[i];
            if(w < item.w, { m(i - 1, w) }, {
                m1 = m(i - 1, w);
                m2 = m(i - 1, w - item.w);
                guard(m1.v >= m2.v + item.v, m1, {
                    (items: append(m2.items, item), w: m2.w + item.w, v: m2.v + item.v)
                })
            })
        })
    };

    // see comment header: should be able to do m = memoize({...})
    // but this reveals a bug in our closure CG, related to fwd decls
    mm = memo(m);

    mm(size(items) - 1, wlim)
};

/*
// knapsack 0/1 solver--recursive w/memoization
// issues: if/elses are too cumbersome
// box ops are too cumbersome
// iskey is necessitated by lack of variants in lookup API
knap01x(items, wlim)
{
    memo = box([:]);

    m(i, w) {
        if(iskey(*memo, (i, w)), { (*memo)[(i, w)] }, {
            res = if(i < 0, { (items: [], w: 0, v: 0) }, {
                item = items[i];
                if(w < item.w, { m(i - 1, w) }, {
                    m1 = m(i - 1, w);
                    m2 = m(i - 1, w - item.w);
                    guard(m1.v >= m2.v + item.v, m1, {
                        (items: append(m2.items, item), w: m2.w + item.w, v: m2.v + item.v)
                    })
                })
            });
            memo <- { mapset($0, (i, w), res) };
            res
        })
    };

    m(size(items) - 1, wlim)
};
*/

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

