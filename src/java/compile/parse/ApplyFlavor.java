package compile.parse;

/**
 * Used to distinguish between instances of {@link compile.term.ApplyTerm}
 * expressing function invocation, collection indexing, and
 * structure addressing.
 */
public enum ApplyFlavor
{
    FuncApp,
    CollIndex,
    StructAddr
}
