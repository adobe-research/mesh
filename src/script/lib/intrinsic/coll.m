//
// collections (lists and maps)
//

/** {@link types.m} */
import * from types;

//
// list
//

/**
 * Append an item to the end of a list.
 * @param x list
 * @param y item to append to the list
 * @return New list with y appended to the end.
 */
intrinsic <T> append(x:[T], y:T) -> [T];

/**
 * @param x Number of items in the list
 * @return A list of Ints [0, ..., n - 1]
 */
intrinsic count(x:Int) -> [Int];

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
 * list concatenation
 * @param x long
 * @param y long
 * @return long value of x + y
 */
intrinsic <T> lplus(x:[T], y:[T]) -> [T];

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
 * Create int list [start, ..., start + extent]
 * @param x start
 * @param y number of entries in the list
 * @return list of integers [start, ..., start + extent]
 */
intrinsic range(x:Int, y:Int) -> [Int];

/**
 * Removes all occurences of an item from a list.
 * @param x list of items
 * @param y item to be removed
 * @return new list with all occurences of item y removed.
 */
intrinsic <T> remove(x:[T], y:T) -> [T];

/**
 * @param x Number of times to repeat item y.
 * @param y value
 * @return A list with the value y repeated x times.
 */
intrinsic <T> rep(x:Int, y:T) -> [T];

/**
 * TODO: empty list throws
 * @param x list of items
 * @return A new list of items x, minus the first value.
 */
intrinsic <T> rest(x:[T]) -> [T];

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
// map
//

/**
 * Create map from key and value lists.
 * For duplicate keys, last instance wins.
 * Equal list length is not required, value
 * list is cycled over if necessary.
 * @param x list of keys
 * @param y list of values
 * @return new map
 */
intrinsic <K,V> assoc(x:[K], y:[V]) -> [K : V];

/**
 * Return the values of a map as a list.
 * @param x map
 * @return List of values in map.
 */
intrinsic <K,V> entries(x:[K : V]) -> [(K, V)];

/**
 * Determine of value is a valid key in a map.
 * @param x map
 * @param y key
 * @return true if y is a valid key in y, otherwise false.
 */
intrinsic <K,V> iskey(x:[K : V], y:K) -> Bool;

/**
 * @param x map
 * @return keyset of a map as a list
 */
intrinsic <K,V> keys(x:[K : V]) -> [K];

/**
 * create a new map with original's contents, minus the given key
 * @param x map
 * @param y key to be removed
 * @return new map with original's contents, minus the given key
 */
intrinsic <K,V> mapdel(x:[K : V], y:K) -> [K : V];

/**
 * Return new map with original's contents, with given key
 * now associated with value.
 * @param x map
 * @param y key
 * @param z new value
 * @return New map with original's contents, with given key  now associated with value.
 */
intrinsic <K, V> mapset(x:[K : V], y:K, z:V) -> [K : V];

/**
 * Create a new map with original's contents, but with
 * given keys associated with values.
 * Note that we roll over the value list.
 * @param x map
 * @param y list of keys
 * @param z list of new values
 * @return New new map with original's contents, but with given keys associated with values.
 */
intrinsic <K,V> mapsets(x:[K : V], y:[K], z:[V]) -> [K : V];

/**
 * Merge two maps. Right map wins where keysets overlap.
 * @param x map
 * @param y map
 * @return New merged map of x and y.
 */
intrinsic <K,V> mplus(x:[K : V], y:[K : V]) -> [K : V];

/**
 * returns the values of a map as a list
 * @param x map
 * @return list containing all of the values from the map
 */
intrinsic <K,V> values(x:[K : V]) -> [V];

