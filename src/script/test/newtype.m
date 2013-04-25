
import * from std;
import * from unittest;

// nominal types are created with the New() type constructor.
// type Nom = New(T) where T is a type expr
// generates a constructor function Nom(v:T) -> Nom,
// and a destructor function _Nom(v:Nom) -> T
// TODO type parameters should work too, don't currently

type CId = New(Int);

c:CId = CId(0);
i:Int = _CId(c);

f(c:CId) { _CId(c) };

assert_equals({1}, {f(c) + 1});

// another new type over Int

type RId = New(Int);

r:RId = RId(0);
j:Int = _RId(r);

g(r:RId) { _RId(r) };

assert_equals({1}, {g(r) + 1});

// fail
//

// f(r)
// g(c)
