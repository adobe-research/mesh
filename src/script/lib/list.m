//
// List utility functions
//

/**
 * @param list list of items to scan
 * @param item value to look for in the list of items
 * @return true if list contains given item.
 */
contains(list, item) { find(list, item) < size(list) };

/**
 * cross product of two lists
 * @param xs list of values
 * @param ys list of values
 * @return cross product of two lists:
 * [(x0, y0), ..., (xN, y0), (x0, y1), ... (xN, yN)]
 */
cross(xs, ys)
{
    xn = size(xs);
    ixs = count(xn * size(ys));
    zip(mapll(ixs | { $0 % xn }, xs), mapll(ixs | { $0 / xn }, ys))
};

/**
 * evaluate f at each position of a list, using the value at that position
 * as the left argument, and the next value as the right argument. Begin with
 * f(init, first(list)).
 * @param init initial value for left argument to f
 * @param f function to be evaluated
 * @param args list of values
 * @param list of values returned by evaluating f over this list of values.
 */
eachpair(init, f, args)
{
    list = [init] + args;
    index(args) | { i => f(list[i], list[i + 1]) }
};

/**
 * returns indexes where list changes value
 * @param list list of values
 * @return returns list indexes where list changes value
 */
edges(list)
{
    [0] + filter(drop(1, index(list)), { list[$0 - 1] != list[$0] })
};

/**
 * create a singleton list from a value
 * @param v value
 * @return a list of one item which is the value v
 */
enlist(v) { [v] };

/**
 * @param list
 * @return a list of the indexes of a list.
 * same as size $ count
 */
index(list) { count(size(list)) };

/**
 * partition a list of items by the value of a function at those items
 * @param vals list of items to be partioned
 * @param f function that partions the list based on return value of this function
 * @return a map with keys that are the return values of f and value is a list of items
 *         from vals that produced the key value when passed into f.
 */
part(vals, f) { group(vals | f, vals) };

/**
 * 
 * @param n number.
 * @param f function.
 * @return a list containing the result of evaluating f() n times.
 */
repeat(n, f) { rep(n, ()) | f };

/**
 * list reverse
 * @param list list of items
 * @return a new list with the items in reverse order
 */ 
reverse(list) {
    n = size(list);
    mapll(range(n - 1, -n), list)
};

/**
 * rotate list
 * @param list list of items
 * @param n number of positons to shift the list, positive value shifts to the right
 *          negative numbers shift to the left
 * @return new list with items rotated
 */ 
rotate(list, n)
{
    if(n >= 0,
        {take(-n, list) + drop(-n, list)},
        {drop(-n, list) + take(-n, list)})
};

/**
 * return list containing lengths of runs of equal values
 * @param list list of values
 * @return list containing lengths of runs of equal values within the list
 */
runlens(list)
{
    eachpair(0, { $1 - $0 }, drop(1, append(edges(list), size(list))))
};

/**
 * group list into runs of adjacent equal items
 * @param list list of values
 * @return list of lists of adjacent equal items that are within the original list
 * @code runs([1,1,4,2,2,2]) returns [[1, 1], [4], [2, 2, 2]] @endcode
 */
runs(list)
{
    cut(list, edges(list))
};

//
// synonyms that might become renames
//
head = first;
tail = rest;
