//
// Map utility functions
//

import reduce from loop;

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
 * Compose a function with a map, yielding a composite function
 * that uses the original function's result to index the map.
 * @code
 * f = compm({ iif($0, #ok, #err) }, [#ok: "OK", #err: "ERR"])
 * f(false)
 * "ERR"
 * @endcode
 *
 * @param x function to determine which map key will be used to access map y
 * @param y map
 * @return Value from map y whose key was determined by function x.
 */
intrinsic <X,K,Y> compm(x:(X -> K), y:[K : Y]) -> (X -> Y);

/**
 * given a list, returns a map from items to counts
 * @param list list of values
 * @return a map with keys that are the unique values in the list and the
 *         key values are count of times the value appeared in the list
 */
counts(list)
{
    inckey(map, key) { mapset(map, key, 1 + mapgetd(map, key, 0)) };
    reduce(inckey, [:], list)
};

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
 * Apply a list of keys to a map of items, yielding a selection list.
 * @param list of keys
 * @param map
 * @return New list of values based on selected keys.
 */
intrinsic <K,V> maplm(x:[K], y:[K : V]) -> [V];

/**
 * Apply a map of arguments to a function, yielding map of results
 * @param x map of arguments
 * @param y function that will be passed map value arguments
 * @return map of results after calling the function y with each map value.
 */
intrinsic <X,Y,Z> mapmf(x:[X : Y], y:(Y -> Z)) -> [X : Z];

/**
 * Apply a map of keys to a map of values, yielding a map of selected values.
 * @param x map of keys to be selected
 * @param y map
 * @return map of selected items from the map y
 */
intrinsic <K,V> mapml(x:[K : Int], y:[V]) -> [K : V];

/**
 * Apply a map of indexes to a list of items, yielding a map of selected items.
 * @param x map of indexes to be selected
 * @param y list of values
 * @return map of selected items from the list y
 */
intrinsic <X,Y,Z> mapmm(x:[X : Y], y:[Y : Z]) -> [X : Z];

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
 * mapz(lists, f) == map(zip(lists), f), but doesn't
 * create the intermediate list of tuples.
 * @param x tuple of lists
 * @param y function
 */
intrinsic <T:[*], X> mapz(x:Tup(T | List), y:(Tup(T) -> X)) -> [X];

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

/**
 * map lookup with default.
 * @param map map data
 * @param key key to look up in map
 * @param default default value to return if key is not in map
 * @return value in map at the specified key if the map contains the key, otherwise return default value.
 */
mapgetd(map, key, default)
{
    guard(!iskey(map, key), default, {map[key]})
};
