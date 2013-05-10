//
// built-in types
// TODO add kind annotation to type decl syntax
//

// kind: *
intrinsic type Bool;        // the usual...
intrinsic type Int;
intrinsic type Long;
intrinsic type Float;
intrinsic type Double;
intrinsic type String;
intrinsic type Symbol;      // #symbol
intrinsic type Opaque;      // used in conjunction with New for FFI hookups
intrinsic type Unit;        // singleton type with value (), sugar (also) ()

// kind: * -> *
intrinsic type Box;         // Box(T), sugar *T (for now)
intrinsic type New;         // type Nom = New(T) creates nominal type Nom over carrier T
intrinsic type List;        // List(T), sugar [T]

// kind: (*, *) -> *
intrinsic type Map;         // Map(K, V), sugar [K:V]
intrinsic type Fun;         // Fun(A, B), sugar A -> B

// kind: [*] -> *
intrinsic type Tup;         // Tup(<type list>), sugar (T1, T2, ...)

// kind: (*, [*]) -> *
intrinsic type Rec;         // Rec(<key type>, <type list>), sugar (k1:T1, k2:T2, ...)
intrinsic type Sum;         // Sum(<key type>, <type list>), sugar TBD

// type transformers
intrinsic type TMap;        // type-level map: TMap(<type list>, <type constructor>)
intrinsic type Index;       // experimental
intrinsic type Assoc;       // experimental

