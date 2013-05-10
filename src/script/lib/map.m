//
// Map utility functions
//

import reduce from loop;

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
