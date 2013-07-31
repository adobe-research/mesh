/* 
 * Pseudo-random generator based on Java Numerical Toolkit (JNT)
 * Random.UniformSequence class.  We do not use Mesh's own rand() so that we
 * can compare results with equivalent Java, C and Fortran codes.
 */

// Creates and returns a new pseudo-random number generator
// If 'seed' is 0, then the generator is seeded using the current time (in millis)
randomgen =
{
    // private constants
    M1 = (1 << 30) + ((1 << 30) - 1);
    M2 = 1 << 16;
    DM1 = 1.0 /. i2f(M1);

    K0 = 9069 % M2;
    K1 = 9069 / M2;

    // function
    { (seed : Int) -> (() -> Double) =>

        // Initialize members (i, j, m)
        actualseed = guard(seed != 0, seed, { l2i(millitime()) });

        makeodd = { guard($0 % 2 != 0, $0, { dec($$0) }) };
        jseed = (min $ makeodd)(abs(actualseed), M1);

        initseeds = {
            js, j0, j1 =>
                nextjseed = j0 * K0;
                (
                    nextjseed,
                    nextjseed % M2,
                    (nextjseed / M2 + j0 * K1 + j1 * K0) % (M2 / 2)
                )
        };

        seeds = tracen((jseed, jseed % M2, jseed / M2), 17, initseeds) |
                { _, j0, j1 => j0 + M2 * j1 };

        m = tail(seeds); // tracen includes the initial value as first element

        state = box(4, 16, m);

        {
            // private utils
            makepositive(n) { guard(n >= 0, n, { n + M1 }) };
            decwrap(n) { guard(n == 0, 16, { dec(n) }) };

            (i, j, m) = *state;

            k = makepositive(m[i] - m[j]);

            state := (decwrap(i),
                        decwrap(j),
                        listset(m, j, k));

            DM1 *. i2f(k)
        }
    }
}();

randomgen_range(seed, left, right)
{
    width = right -. left;
    r = randomgen(seed);
    {
        left +. run(r) *. width;
    }
};
