
import sort;
import bench;
import unittest;

// benchmark merge sort from sort.m

//
// test runner - run the sort, check sortedness and population of result list
//
check(list, diff)
{
    sorted = sort(list, diff);

    (sorted: !any(count(size(list) - 1), { diff(sorted[$0 + 1], sorted[$0]) < 0 }),
     isperm: isperm(list, sorted),
     first10: take(10, sorted))
};

//
// test runs on ints
//

print("ints");

randnums(n) { draw(n, n / 3) };

nlist = randnums(512000);

// print some info
print(size: size(nlist), first10: take(10, nlist));

asc = check(nlist, (-));
assert_equals({true}, {asc.sorted});
assert_equals({true}, {asc.isperm});

desc = check(nlist, { $1 - $0 });
assert_equals({true}, {desc.sorted});
assert_equals({true}, {desc.isperm});

benchn(5, { printstr("."); sort(nlist, (-)) });

assert_equals({ sort(nlist, (-)) }, { mapll(isort(nlist, (-)), nlist) });

//
// test runs on strings
//

print("strings");

wordlist = strsplit(readfile("data/wordlist.txt"), " ");

randwords(n) { mapll(draw(n, size(wordlist)), wordlist) };

slist = randwords(512000);

// print some info
print(size: size(slist), first10: take(10, slist));

asc_str = check(slist, strcmp);
assert_equals({true}, {asc_str.sorted});
assert_equals({true}, {asc_str.isperm});

desc_str = check(slist, { strcmp($1, $0) });
assert_equals({true}, {desc_str.sorted});
assert_equals({true}, {desc_str.isperm});

benchn(5, { printstr("."); sort(slist, strcmp) });

assert_equals({ sort(slist, strcmp) }, { mapll(isort(slist, strcmp), slist) });
