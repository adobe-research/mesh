/**
	SciMark2: A Java numerical benchmark measuring performance
	of computational kernels for FFTs, Monte Carlo simulation,
	sparse matrix computations, Jacobi SOR, and dense LU matrix
	factorizations.  
*/

import * from std;

import Constants;
import * from Random;
import * from Kernel;

/* Benchmark 5 kernels with individual Mflops.
 "results[0]" has the average Mflop rate.

*/

// default to the (small) cache-contained version

min_time = Constants.RESOLUTION_DEFAULT;

FFT_size = Constants.FFT_SIZE;
SOR_size = Constants.SOR_SIZE;
Sparse_size_M = Constants.SPARSE_SIZE_M;
Sparse_size_nz = Constants.SPARSE_SIZE_nz;
LU_size = Constants.LU_SIZE;

// look for runtime options
/*
Standalone javascript engines may not take commandline arguments.
if (args.length > 0)
{

    if (args[0].equalsIgnoreCase("-h") || 
                args[0].equalsIgnoreCase("-help"))
    {
        print("Usage: [-large] [minimum_time]");
        return;
    }

    int current_arg = 0;
    if (args[current_arg].equalsIgnoreCase("-large"))
    {
        FFT_size = Constants.LG_FFT_SIZE;
        SOR_size =  Constants.LG_SOR_SIZE;
        Sparse_size_M = Constants.LG_SPARSE_SIZE_M;
        Sparse_size_nz = Constants.LG_SPARSE_SIZE_nz;
        LU_size = Constants.LG_LU_SIZE;

        current_arg++;
    }

    if (args.length > current_arg)
        min_time = Double.valueOf(args[current_arg]).doubleValue();
}
*/

// run the benchmark

R = randomgen(Constants.RANDOM_SEED);

kernel = Kernel();

res = [ 
    kernel.measureFFT(FFT_size, min_time, R),
    kernel.measureSOR(SOR_size, min_time, R),
    kernel.measureMonteCarlo(min_time, R),
    kernel.measureSparseMatmult(Sparse_size_M, Sparse_size_nz, min_time, R),
    kernel.measureLU( LU_size, min_time, R)
];

average = reduce(plus, 0.0, res) /. i2f(size(res));

// print out results
print();
print("SciMark 2.0a");
print();
print("Composite Score: " + f2s(average));
if (res[0] == 0.0, {
    print("FFT: ERROR, INVALID NUMERICAL RESULT!")
}, {
    print("FFT (" + i2s(FFT_size) + "): " + f2s(res[0]))
});

print("SOR (" + i2s(SOR_size) + "x" + i2s(SOR_size) + "): " + f2s(res[1]));
print("Monte Carlo : " + f2s(res[2]));
print("Sparse matmult (N=" + i2s(Sparse_size_M) + ", nz=" + i2s(Sparse_size_nz) + "): " + f2s(res[3]));
if (res[4] == 0.0, {
    print("LU: ERROR, INVALID NUMERICAL RESULT!")
}, {
    print("LU (" + i2s(LU_size) + "x" + i2s(LU_size) + "): " + f2s(res[4]))
});
