//
// String functions
//

/**
 * {@link String#endsWith(String)}
 * @param x base string
 * @param y suffix string
 * @return true if string x ends with string y
 */
intrinsic endswith(x:String, y:String) -> Bool;

/**
 * {@link String#startsWith(String)}
 * @param x base string
 * @param y prefix string
 * @return true if string x starts with string y
 */
intrinsic startswith(x:String, y:String) -> Bool;

/**
 * concatenate strings
 * @param x list of strings
 * @return a new string containing all of the strings in x concatenated together
 */
intrinsic strcat(x:[String]) -> String;

/**
 * compare strings lexicographically
 * {@link String#compareTo(String)}
 * @param x string
 * @param y string
 * @return Return 0 if strings are lexicographically equal, less than zero if string x
 *         is lexicographically less than string y, and greater than zero if string x
*          is lexicographically greater than string x.
*/
intrinsic strcmp(x:String, y:String) -> Int;

/**
 * @param x string value
 * @param y list in indexes
 * @return A list of strings based on original string x but sliced at provided index points.
 */
intrinsic strcut(x:String, y:[Int]) -> [String];

/**
 * Drops first x chars if x > 0, last -x if x < 0.
 * n is held to string size.
 *
 * @param x Number of chars to drop from the string.
 *          x > 0 drops first x chars
 *          x < 0 drops x chars from the end of the string
 *          x is held to the length of the string, x > y.length() will be treated as y.length()
 * @param y string
 * @return new string with specified amount of characters dropped
 */
intrinsic strdrop(x:Int, y:String) -> String;

/**
 * @param x base string
 * @param y search string
 * @return returns the location of the search string within the base string,
 *         otherwise returns the length of the base string if search string is not found.
 */
intrinsic strfind(x:String, y:String) -> Int;

/**
 * Joins a list of strings using the separator.
 * @param x list of strings
 * @param y separator
 * @return new string 
 */
intrinsic strjoin(x:[String], y:String) -> String;

/**
 * @param x string
 * @return length of string
 */
intrinsic strlen(x:String) -> Int;

/**
 * @param x base string
 * @param y regular expression delimiter
 * @return List of strings based on splitting the base string using the regular expression
 */
intrinsic strsplit(x:String, y:String) -> [String];

/**
 * Takes first x chars if x > 0, last -x if x < 0.
 * If x > than y.length() then wraps around to begining of y.
 *
 * @param x Number of chars to take from the string.
 *          x > 0 takes first x chars
 *          x < 0 takes x chars from the end of the string
 *          If x > than y.length() then wraps around to begining of y.
 * @param y string
 * @return new string with specified number of chars from the original string
 */
intrinsic strtake(x:Int, y:String) -> String;

/**
 * @param x base string
 * @param y function that accepts each character of the string x and returns a boolean
 *           value for whether or not to return the position of the character in the string
 * @return List of indexes in the base string where the predicate function returned a true value.
 */
intrinsic strwhere(x:String, y:String -> Bool) -> [Int];

/**
 * @param x base string
 * @param y start index
 * @param z length
 * @return a new substring of the base string, starting at index y and an end point of y + z
 */
intrinsic substr(x:String, y:Int, z:Int) -> String;

/** 
 * convert a Symbol to a string
 * @param x Symbol value
 * @return string representation of the symbol x.
 */
intrinsic sym2s(x:Symbol) -> String;

/**
 * @param x string to convert
 * @return the string x converted to lowercase
 */
intrinsic tolower(x:String) -> String;

/**
 * @param x string to convert
 * @return the string x converted to uppercase
 */
intrinsic toupper(x:String) -> String;
