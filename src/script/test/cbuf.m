
import * from std;
import * from unittest;

// concurrent access to mutable struct through ffi

// Transactions are inevitable once all necessary relationships
// are established. This means that a box can be used as a read-
// write lock to coordinate access with an external resource, as
// shown below (for example).
//
// NOTE: currently nested transactions are simply merged, breaking
// inevitability in the general case. Even real nested transactions
// will only preserve inevitability of outer transactions, and inner
// transactions appearing in the inevitable part of their ancestors--
// but not inner transactions in the relationship-establishing part
// of their ancestors. TODO full writeup of this.

// array, aset, aget is racy FFI to flat array
// make concurrent buffer using box as r/w lock

cbuf(n, v)
{
    data = array(n, v);

    lock = box();

    read(i)
    {
        do { get(lock); aget(data, i) }
    };

    write(i, v)
    {
        do { own(lock); aset(data, i, v); () }
    };

    _do(f)
    {
        do { own(lock); f() }
    };

    (#read: read, #write: write, #do: _do)
};

LEN = 10;
NTASKS = 100;

buf = cbuf(LEN, 0);

bump(n)
{
    buf.do({ buf.write(n, buf.read(n) + 1) })
};

pfor(flatten(count(LEN) | { rep(NTASKS, $0) }), bump);

buf.do({ print(count(LEN) | buf.read) });

assert_equals({ rep(LEN, NTASKS) }, { count(LEN) | buf.read });

