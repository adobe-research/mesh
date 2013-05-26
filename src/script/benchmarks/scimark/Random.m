/* 
 * Pseudo-random generator based on Java Numerical Toolkit (JNT)
 * Random.UniformSequence class.  We do not use Mesh's own rand() so that we
 * can compare results with equivalent Java, C and Fortran codes.
 */

import * from std;
export .;

// Creates and returns a new pseudo-random number generator 
// If 'seed' is 0, then the generator is seeded using the current time (in millis)
randomgen(seed:Int) -> (() -> Double)
{
    members = box(#i:0, #j:0, #m:[0]);

    // constants
    m1 = pow(2, 30) + (pow(2, 30) - 1);
    m2 = pow(2, 16);
    dm1 = 1.0 /. i2f(m1);

    // Initialize members (i, j, m)
    k0 = 9069 % m2;
    k1 = 9069 / m2;

    actualseed = guard(seed != 0, seed, { l2i(millitime()) }); 

    makeodd = { guard($0 % 2 != 0, $0, { dec($$0) }) };
    jseed = (min $ makeodd)(abs(actualseed), m1);

    initseeds = { 
        js, j0, j1 => 
            nextjseed = j0 * k0; 
            ( 
                nextjseed, 
                nextjseed % m2, 
                (nextjseed / m2 + j0 * k1 + j1 * k0) % (m2 / 2)
            ) 
    };

    seeds = tracen((jseed, jseed % m2, jseed / m2), 17, initseeds) |
            { _, j0, j1 => j0 + m2 * j1 };

    m = rest(seeds); // tracen includes the initial value as first element 

    members := (#i:4, #j:16, #m:m);

    // private utils
    makepositive = { guard($0 >= 0, $0, { $$0 + m1 }) };
    decwrap = { guard($0 == 0, 16, { dec($$0) }) };

    { 
        this = *members;

        k = makepositive(this.m[this.i] - this.m[this.j]);

        members := (#i: decwrap(this.i), 
                    #j: decwrap(this.j), 
                    #m: listset(this.m, this.j, k));

        dm1 *. i2f(k)
    }
};

randomgen_range(seed, left, right)
{
    width = right -. left;
    r = randomgen(seed);
    {
        left +. run(r) *. width;
    }
};
