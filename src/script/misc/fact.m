
// factorial

n = 10;

// recurse
fact0(n) {
    guard(n == 0, 1, {n * fact0(n - 1)})
};

// reduce
fact1(n) {
    reduce((*), 1, range(1, n))
};

// iterate
fact2(n) {
    next(i, j) { (i + 1, i * j) };
    cyclen((1, 1), n, next).1
};

print(fact0(n), fact1(n), fact2(n));
