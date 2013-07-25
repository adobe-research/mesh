
// max continuous subsequence of a nonempty list of ints.
// interesting cases include negative numbers in the list.
// for illustration, return value is triple of
// - input array
// - parallel array of (start index, total) for optimal
//   subsequence at each position
// - winning subsequence
// e.g.
// > mvcs(draw(5, 100) @- 50)
// ([-3, -45, 6, 37, -46], [(0, -3), (1, -45), (2, 6), (2, 43), (2, -3)], [6, 37])
//
mvcs(a) {
    // compute b parallel to a, with element b[i] = (start, sum)
    // of max subsequence ending at i.
    // transition function implements the decision to extend
    // previous optimal window, or start a new one.
    // also track winning position
    (infos, winner) = reduce({ state, e =>
        (infos, winner) = state;
        (prev_start, prev_tot) = last(infos);
        i = size(infos);
        info = if(prev_tot > 0, {(prev_start, prev_tot + e)}, {(i, e)});
        (append(infos, info), iif(info.1 > infos[winner].1, i, winner))
    }, ([(0, first(a))], 0), rest(a));

    // return input, info array and winning subsequence
    (a, infos, mapll(fromto(infos[winner].0, winner), a))
};

