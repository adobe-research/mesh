
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
mvcs(a) {
    // compute b parallel to a, with b[i] = (start, sum) of max
    // subsequence ending at i.
    // reducer implements the decision at i to extend i-1's max
    // subsequence, or start a new one.
    // also tracks and returns (position of) winning subsequence.
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

