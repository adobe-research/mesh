

/**
 Estimate Pi by approximating the area of a circle.
 
 How: generate N random numbers in the unit square, (0,0) to (1,1)
 and see how are within a radius of 1 or less, i.e.
 <pre>
 
 sqrt(x^2 + y^2) < r
 
 </pre>
 since the radius is 1.0, we can square both sides
 and avoid a sqrt() computation:
 <pre>
 
 x^2 + y^2 <= 1.0
 
 </pre>
 this area under the curve is (Pi * r^2)/ 4.0,
 and the area of the unit of square is 1.0,
 so Pi can be approximated by
 <pre>
 # points with x^2+y^2 < 1
 Pi =~              --------------------------  * 4.0
 total # points
 
 </pre>
*/

import Random qualified;

num_flops(num_samples)
{   
    // 3 flops in x^2+y^2 and 1 flop in random routine
    i2f(num_samples) *. 4.0 
};

integrate(num_samples)
{
    SEED = 113;

    R = Random.randomgen(SEED);

    under = cyclen(0, num_samples, { n =>
        x = R();
        y = R();

        guard((x *. x) +. (y *. y) >. 1.0, n, { inc(n) })
    });

    i2f(under) /. i2f(num_samples) *. 4.0
};
