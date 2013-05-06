//
// List utility functions
//

/**
 * Append an item to the end of a list.
 * @param x list
 * @param y item to append to the list
 * @return New list with y appended to the end.
 */
intrinsic <T> append(x:[T], y:T) -> [T];

/**
 * Compose a function with a list, yielding a composite function
 * that uses the original function's result to index the list.
 * @code
 * f = compl({ $0 % 3 }, ["One", "Two", "Three"])
 * f(100)
 * "Two"
 * @endcode
 *
 * @param x function to determine which list index 
 * @param y list of values
 * @return Value from list y whose index was determined by function x.
 */
intrinsic <X,Y> compl(x:(X -> Int), y:[Y]) -> (X -> Y);

/**
 * @param list list of items to scan
 * @param item value to look for in the list of items
 * @return true if list contains given item.
 */
contains(list, item) { find(list, item) < size(list) };

/**
 * @param x Number of items in the list
 * @return A list of Ints [0, ..., n - 1]
 */
intrinsic count(x:Int) -> [Int];

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
 * Cut the given list into sublist, at the given indexes.
 * Indexes must be between 0 and size(list), in ascending
 * order. Duplicate indexes are allowed and will produce
 * an empty sublist for the earlier instance(s) of the duplicate.
 * A final index equal to size(list) will produce an empty final
 * sublist.
 * @param list list of items
 * @param cutpoints list of index cut points
 * @return a list of sublists, as described above
 */
intrinsic <T> cut(list : [T], cutpoints : [Int]) -> [[T]];

/**
 * @param x List of items.
 * @return Sublist of unique items from list.
 */
intrinsic <T> distinct(x:[T]) -> [T];

/**
 * Drops first x if x > 0, last -x if x < 0 items from list.
 * n is held to list size.
 *
 * @param x Number of items to drop from the list.
 *          x > 0 drops first x items
 *          x < 0 drops x items from the end of the list
 *          x is held to the length of the list, x > size(y) will be treated as size(y)
 * @param y List of of values
 * @return Sublist of values.
 */
intrinsic <T> drop(x:Int, y:[T]) -> [T];

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
 * Filter a list of items.
 * @param x list of items
 * @param y Predicate function to determine if list item should be returned.
 * @returns Sublist of x where predicate returned true.
 */
intrinsic <T> filter(x:[T], y:(T -> Bool)) -> [T];

/**
 *
 * @param x List of values.
 * @param y Value to search for.
 * @return Index of first occurence of item in list, or list size
 */
intrinsic <T> find(x:[T], y:T) -> Int;

/**
 * TODO: empty list throws, currently.
 * @param x List
 * @return First element of non-empty list.
 */
intrinsic <T> first(x:[T]) -> T;

/**
 * Concatenate multiple lists together.
 * @param x List of lists
 * @returns Concatenated lists.
 */
intrinsic <T> flatten(x:[[T]]) -> [T];

/**
 * Create list of ints [from..to], ascending or descending
 * @param x start value of the list, if x is greater than y then
 *          list will be in descending order.
 * @param y end value of the list
 * @return sequential list of int values.
 */
intrinsic fromto(x:Int, y:Int) -> [Int];

/**
 * group by - given a list of keys and a list of values,
 * returns a map from keys to collections of values.
 * Note that we roll over the key list.
 *
 * @param x list of keys
 * @param y list of values
 * @return A map from keys to collections of values.
 */
intrinsic <K,V> group(x:[K], y:[V]) -> [K : [V]];

/**
 * @param list
 * @return a list of the indexes of a list.
 * same as size $ count
 */
index(list) { count(size(list)) };

/**
 * Determine of value is a valid list index.
 * @param x int value
 * @param y list of values
 * @return true if x is a valid index of y, otherwise false.
 */
intrinsic <T> isindex(x:Int, y:[T]) -> Bool;

/**
 * TODO: empty list throws
 * @param x List
 * @return Last element of non-empty list.
 */
intrinsic <T> last(x:[T]) -> T;

/**
 * Return new list with original list's contents,
 * but with value at index replaced.
 * @param x list of values
 * @param y index
 * @param z new value
 * @return New list with original list's contents, but with value at index replaced.
 */
intrinsic <T> listset(x:[T], y:Int, z:T) -> [T];

/**
 * Return new list with original list's contents,
 * but with values at indexes replaced.
 * Note that we roll over the value list.
 * @param x list of values
 * @param y list of indexes
 * @param z list of new values
 * @return New list with original list's contents, but with values at indexes replaced.
 */
intrinsic <T> listsets(x:[T], y:[Int], z:[T]) -> [T];

/**
 * Apply a list of indexes to a list of items, yielding a selection list.
 * @param list of indexes
 * @param list of values
 * @return New list of values based on selected indexes.
 */
intrinsic <T> mapll(x:[Int], y:[T]) -> [T];

/**
 * partition a list of items by the value of a function at those items
 * @param vals list of items to be partioned
 * @param f function that partions the list based on return value of this function
 * @return a map with keys that are the return values of f and value is a list of items
 *         from vals that produced the key value when passed into f.
 */
part(vals, f) { group(vals | f, vals) };

/**
 * Removes all occurences of an item from a list.
 * @param x list of items
 * @param y item to be removed
 * @return new list with all occurences of item y removed.
 */
intrinsic <T> remove(x:[T], y:T) -> [T];

/**
 * 
 * @param n number.
 * @param f function.
 * @return a list containing the result of evaluating f() n times.
 */
repeat(n, f) { rep(n, ()) | f };

/**
 * TODO: empty list throws
 * @param x list of items
 * @return A new list of items x, minus the first value.
 */
intrinsic <T> rest(x:[T]) -> [T];

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

/**
 * @param x list
 * @return Shuffled list
 */
intrinsic <T> shuffle(x:[T]) -> [T];

/**
 * @param x list of values
 * @return the number of items in the list
 */
intrinsic <T> size(x:[T]) -> Int;

/**
 * Takes first x items from list if x > 0, last -x if x < 0.
 * If x > than size(y) then wraps around to begining of y.
 *
 * @param x Number of items to take from the list.
 *          x > 0 takes first x items
 *          x < 0 takes x items from the end of the list
 *          If x > than size(y) then wraps around to begining of y.
 * @param y list of items
 * @return new list with specified number of items from the original list
 */
intrinsic <T> take(x:Int, y:[T]) -> [T];

/**
 * Return sublist of unique items from list.
 * @param x list of items
 * @return list of unique items in list x
 */
intrinsic <T> unique(x:[T]) -> [T];

/**
 *
 * @param x list of tuples
 * @return tuple of lists
 */
intrinsic <Ts:[*]> unzip(x:[Tup(Ts)]) -> Tup(Ts | List);

/**
 * @param x base list of values
 * @param y function that accepts each item in the list x and returns a boolean
 *           value for whether or not to return the position of the item in the list
 * @return List of indexes in the base list where the predicate function returned a true value.
 */
intrinsic <T> where(x:[T], y:T -> Bool) -> [Int];

/**
 * 
 * @param x tuple of lists of values
 * @return list of tuples
 */
intrinsic <Ts:[*]> zip(x:Tup(Ts | List)) -> [Tup(Ts)];

//
// synonyms that might become renames
//
head = first;
tail = rest;
