//
// Set-like operations that require list nad map capabilities
//

import * from list;
import * from map;
import rand, sign from math;
import cycle from loop;

/**
 * set intersection on lists
 * @param list1 list of values
 * @param list2 list of values
 * @return list containing common values in both lists
 */
intersection(list1, list2)
{
    map2 = assoc(list2, [()]);
    unique(filter(list1, { iskey(map2, $0) }))
};

/**
 * @param a list of values
 * @param b list of values
 * @return true if lists a and b are permutations of each other
 */
isperm(a, b) { counts(a) == counts(b) };

/**
 * directional set difference on lists
 * @param list1 list of values
 * @param list2 list of values
 * @return list containing values that are unique to each list
 */
difference(list1, list2)
{
    map2 = assoc(list2, [()]);
    unique(filter(list1, { !iskey(map2, $0) }))
};

/**
 * merge sort - parallelization based on availprocs().
 * cmp(l, r) returns an int with the same sign as the difference between l and r.
 * e.g. use (-) for an ascending sort on ints.
 * @param lst list of values to sort
 * @param cmp function that accepts l and r values from the list and returns an int with the same sign as the difference between l and r.
 * @return list with elements sorted based on cmp
 */
sort(lst, cmp)
{
    merge(a, b)
    {
        (asize, bsize) = (size(a), size(b));
        (merged, asuf, bsuf) = cycle(
            { m, i, j => i < asize && {j < bsize} },
            ([], 0, 0),
            { m, i, j =>
                if(cmp(a[i], b[j]) <= 0,
                    { (append(m, a[i]), i + 1, j) },
                    { (append(m, b[j]), i, j + 1) })
            });
        merged + drop(asuf, a) + drop(bsuf, b)
    };

    subsort(lst, njobs)
    {
        lsize = size(lst);
        guard(lsize <= 1, lst, {
            half = lsize / 2;
            subn = njobs / 2;
            args = [(take(half, lst), subn), (drop(half, lst), subn)];
            subs = if(njobs > 1, { args |: subsort }, { args | subsort });
            merge(subs[0], subs[1])
        })
    };

    subsort(lst, availprocs())
};

/**
 * index sort.
 * instead of returning a sorted version of the list, return a sorted
 * list of indexes: sort(list, cmp) == mapll(isort(sort, cmp), list)
 * @param lst list of values to sort
 * @param cmp function that accepts l and r values from the list and returns an int with the same sign as the difference between l and r.
 * @return list of sorted indexes into list, base on cmp
 */
isort(lst, cmp) {
    sort(index(lst), { cmp(lst[$0], lst[$1]) })
};

/**
 * index qsort.
 * @param lst list of values to sort
 * @param cmp function that accepts l and r values from the list and returns an int with the same sign as the difference between l and r.
 * @return list of sorted indexes into lst, base on cmp
 */
iqsort(lst, cmp) {
   qsort(index(lst), { cmp(lst[$0], lst[$1]) })
};

/**
 * functional qsort - subsort parallelization based on availprocs().
 * diff(l, r) returns an int with the same sign as the difference between l and r.
 * e.g. use (-) for an ascending sort on ints.
 * note: partitioning is not parallelized. provides no speedup, not sure why not.
 * @param list list of values to sort
 * @param cmp function that accepts l and r values from the list and returns an int with the same sign as the difference between l and r.
 * @return list with elements sorted based on cmp
 */
qsort(lst, cmp)
{
    subsort(lst, njobs)
    {
        guard(size(lst) <= 1, lst, {
            pivot = lst[rand(size(lst))];
            pivcmp(val) { sign(cmp(val, pivot)) };
            parts = [-1:[], 0:[], 1:[]] + part(lst, pivcmp);
            subn = njobs / 2;
            args = [(parts[-1], subn), (parts[1], subn)];
            subs = if(njobs > 1, { args |: subsort }, { args | subsort });
            subs[0] + parts[0] + subs[1]
        })
    };

    subsort(lst, availprocs() + 2)
};


/**
 * set union on lists
 * @param list1 list of values
 * @param list2 list of values
 * @return list of values consisting of the union of both lists
 */
union(list1, list2)
{
    unique(list1 + list2)
};

