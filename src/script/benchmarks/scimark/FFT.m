/** Computes FFT's of complex, double precision data where n is an integer power of 2.
 * This appears to be slower than the Radix2 method,
 * but the code is smaller and simpler, and it requires no extra storage.
 * <P>
 *
 * @author Bruce R. Miller bruce.miller@nist.gov,
 * @author Derived from GSL (Gnu Scientific Library),
 * @author GSL's FFT Code by Brian Gough bjg@vvv.lanl.gov
 */

/* See {@link ComplexDoubleFFT ComplexDoubleFFT} for details of data layout.
*/

import * from std;

FFT() { 

    num_flops(N:Int) -> Double {
        Nd = i2f(N);
        logN = ln(Nd) /. ln(2.0);
    
        (5.0 *. Nd -. 2.0) *. logN + 2.0 *. (Nd + 1.0)
    };
    
    // TODO: this is ugly -- fix this when we have shift or integer log2 ops!
    int_log2(n) {
        f2i(ln(i2f(n)) /. ln(2.0))
    };
    
    bitreverse(data:[Double]) -> [Double] {
        // Create a Gold-Rader reversal mapping
        n = size(data) / 2;
        ( _, __, rmap ) = cyclen(n - 2, (0, 0, count(n)), { i, j, m =>
            k = n / 2;
            // swap entries in map (maybe)
            mapping = if(i < j, 
                { m | { guard($0 == i, j, { iif($$0 == j, i, $$0) }) } }, 
                { m });
    
            // Calculate next j
            ( nk, nj ) = cycle({ $0 <= $1 }, ( k, j ), { ( $0 / 2, $1 - $0 ) });
            ( i + 1, nj + nk, mapping )
        });
    
        // Apply mapping to data (which is complex, so one mapping entry represents
        // two doubles)
        index(data) | { data[rmap[$0 / 2] * 2 + ($0 % 2)] }
    };

    // TODO: This is implemented as in-place replacement in an array of boxes.
    // Might want to consider a non-mutating algorithm. 
    // TODO: This infinite loops when passed an array size that is not a power of 2.
    transform_internal(data:[Double], direction:Int) -> [Double] {
        N = size(data);
        n = N/2;
        PI = 3.14159265;
        
        guard(n == 1 || { N == 0 }, data, {
            logn = int_log2(n);
            D = bitreverse(data) | box;
    
            cyclen(logn, ( 0, 1 ), { bit, dual =>
                w_real = 1.0;
                w_imag = 0.0;

                theta = 2.0 *. i2f(direction) *. PI /. (2.0 *. i2f(dual));
                s = sin(theta);
                t = sin(theta /. 2.0);
                s2 = 2.0 *. t *. t;
    
                cycle({ $0 < n }, 0, { b => 
                    i = 2*b;
                    j = 2*(b + dual);
    
                    wd_real = *D[j];
                    wd_imag = *D[j+1];
    
                    D[j] := *D[i] -. wd_real;
                    D[j+1] := *D[i+1] -. wd_imag;
                    D[i] := *D[i] + wd_real;
                    D[i+1] := *D[i+1] + wd_imag;
    
                    b + (2 * dual)
                });
    
                cyclen(dual - 1, ( 1, w_real, w_imag ),
                    { a, curr_real, curr_imag  => 
                        // Trignometric recurrence for w^(i * theta) 
                        wreal = curr_real -. s *. curr_imag -. s2 *. curr_real;
                        wimag = curr_imag + s *. curr_real -. s2 *. curr_imag;

                        cycle( { $0 < n }, 0, { b =>  
                            i = 2 * (b + a);
                            j = 2 * (b + a + dual);
    
                            
                            z1_real = *D[j];
                            z1_imag = *D[j + 1];
    
                            wd_real = wreal *. z1_real -. wimag *. z1_imag;
                            wd_imag = wreal *. z1_imag + wimag *. z1_real;
    
                            D[j] := *D[i] -. wd_real;
                            D[j+1] := *D[i+1] -. wd_imag;
                            D[i] := *D[i] + wd_real;
                            D[i+1] := *D[i+1] + wd_imag;
    
                            b + (2 * dual)
                        });
                        ( a + 1, wreal, wimag ) });
    
                ( bit + 1, dual * 2 )
            });
            D | get
        })
    };
    
    transform(data:[Double]) -> [Double] {
        transform_internal(data, -1)
    };
    
    inverse(data:[Double]) -> [Double] {
        n = size(data) / 2;
        norm = 1.0 /. i2f(n);
    
        D = transform_internal(data, 1);
        D | { $0 *. norm }
    };

    test(data:[Double]) -> Double {
        transformed = inverse(transform(data));

        rms_diff(accum:Double, values:(Double,Double)) {
            diff = values.0 -. values.1;
            accum + diff *. diff
        };
        
        sqrt(reduce(rms_diff, 0.0, zip(data, transformed)))
    };
    
    ( #num_flops: num_flops, #transform: transform, #inverse: inverse, #test: test )
};
