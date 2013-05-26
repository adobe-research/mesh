//
// Sorting and related functionality
//

/**
 * given a list of ascending Int values and a single value of the same type,
 * return the insertion point for the single value. Useful for range-based
 * partitioning and finding next-closest values in sparse sorted lists.
 * Note: this impl is just a placeholder until interfaces come along.
 *
 * TODO binary version
 * TODO parameterize on Sortable interface once it exists
 *
 * @param list list of ascending Int values
 * @param val Int value to be inserted
 * @return insertion point in list for the single value
 */
insertion_point(val, list)
{
    first_where({ val <= $0 }, list)
};

/**
 * index qsort.
 * @param lst list of values to sort
 * @param cmp function that accepts l and r values from the list and returns an int with the same sign as the difference between l and r.
 * @return list of sorted indexes into lst, base on cmp
 */
iqsort(lst, cmp)
{
   qsort(index(lst), { cmp(lst[$0], lst[$1]) })
};

/**
 * index sort.
 * instead of returning a sorted version of the list, return a sorted
 * list of indexes: sort(list, cmp) == mapll(isort(sort, cmp), list)
 * @param lst list of values to sort
 * @param cmp function that accepts l and r values from the list and returns an int with the same sign as the difference between l and r.
 * @return list of sorted indexes into list, base on cmp
 */
isort(lst, cmp)
{
    sort(index(lst), { cmp(lst[$0], lst[$1]) })
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
            ([], 0, 0),
            { m, i, j => i < asize && {j < bsize} },
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
