
// just a local impl of map-reduce harness.
// TODO fun distributed version PENDING VARIANTS argh

import * from std;

<K1, V1, K2, V2, K3, V3, K4, V4>
mapred(
    inputs : [(K1, V1)],
    mapper : (K1, V1) -> [(K2, V2)],
    combiner : (K2, [V2]) -> [(K3, V3)],
    mjobs : Int,
    reducer : (K3, [V3]) -> [(K4, V4)],
    rjobs : Int)
{
    mapnode(inputs)
    {
        mapped = flatten(inputs | mapper);
        grouped = entries(group(unzip(mapped)));
        flatten(grouped | combiner)
    };

    results = flatten(chunks(inputs, mjobs) |: mapnode);
    grouped = group(unzip(results));
    reduced = pmapn(entries(grouped), reducer, rjobs);
    flatten(reduced)
};

// ---

splitline(lnum, line)
{
    words = strsplit(line, "\\W+");
    zip(filter(words, empty $ not), [1])
};

sumcount(word, count)
{
    [(word, sum(count))]
};

lines = strsplit(tolower(readfile("data/big.txt")), "\n");
input = zip(index(lines), lines);

print("1 map task, 1 reduce task:");
print(benchn(10, {mapred(input, splitline, sumcount, 1, sumcount, 1)}));

print("4 map tasks, 2 reduce tasks:");
print(benchn(10, {mapred(input, splitline, sumcount, 4, sumcount, 2)}));

print("16 map tasks, 4 reduce tasks:");
print(benchn(10, {mapred(input, splitline, sumcount, 16, sumcount, 4)}));

print("diff task configs equal:");
m1 = mapred(input, splitline, sumcount, 1, sumcount, 1);
m4 = mapred(input, splitline, sumcount, 4, sumcount, 4);
print(m1 == m4);

// ---

// lines1 = [(1, "hello world goodbye world"), (2, "hello hadoop goodbye hadoop")];
// input1 = mapred(lines1, splitline, sumcount);

