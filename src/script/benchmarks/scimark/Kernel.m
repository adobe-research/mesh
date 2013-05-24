
import * from std;

import FFT qualified;
import SOR qualified;
import MonteCarlo qualified;
import SparseCompRow qualified;
import LU qualified;

import Constants qualified;
import Stopwatch;

export measureFFT,measureSOR,measureMonteCarlo,
       measureSparseMatmult,measureLU;

iterate_until(mintime, benchmark) -> (Int,Double) {
    Q = Stopwatch();
    cycles = cycle( { _ => Q.read() <. mintime }, 1, 
        { c =>
          Q.start();
          benchmark(c);
          Q.stop();
          c * 2
        });
    ( cycles, Q.read() )
};

repeat_until(mintime, benchmark) -> (Int, Double) {
    iterate_until(mintime, { c =>
       cyclen(c, (), benchmark) 
    })
};

flops(nf, size, cycles, elapsed) {
    nf(size) *. i2f(cycles) /. elapsed *. fpow(10.0,-6.0)
};

measureFFT(N, mintime, R) {
    x = repeat(2*N, R);

    (cycles, time) = repeat_until(mintime, 
        { FFT.inverse(FFT.transform(x)); () });

    EPS = fpow(10.0, -10.0);

    if (FFT.test(x) /. i2f(N) >. EPS, { 0.0 }, 
        { flops(FFT.num_flops, N, cycles, time) });
};

measureSOR(N, mintime, R) {
    G = repeat(N, { repeat(N, R) } );

    (cycles, time) = iterate_until(mintime, 
        { c => SOR.execute(N, N, 1.25, G, c); () });

    if (Constants.SCIMARK_DEBUG, { print(G) }, { () });
    flops(SOR.num_flops, (N, N, cycles), 1, time)
};

measureMonteCarlo(mintime, R) {
    (cycles, time) = iterate_until(mintime, { c => MonteCarlo.integrate(c); });
    flops(MonteCarlo.num_flops, cycles, 1, time);
};

measureSparseMatmult(N, nz, mintime, R) {

		// initialize square sparse matrix
		//
		// for this test, we create a sparse matrix wit M/nz nonzeros
		// per row, with spaced-out evenly between the begining of the
		// row to the main diagonal.  Thus, the resulting pattern looks
		// like
		//             +-----------------+
		//             +*                +
		//             +***              +
		//             +* * *            +
		//             +** *  *          +
		//             +**  *   *        +
		//             +* *   *   *      +
		//             +*  *   *    *    +
		//             +*   *    *    *  + 
		//             +-----------------+
		//
		// (as best reproducible with integer artihmetic)
		// Note that the first nr rows will have elements past
		// the diagonal. 
    x = repeat(N, R);
    nr = (nz / N); // average number of nonzeros per row
    anz = nr * N; // _actual_ number of nonzeros
    val = repeat(anz, R);

    rows = count(N+1) @* nr;
    cols = flatten(count(N) | { r =>
        step = iif(r / nr < 1, 1, r / nr); // take at least unit steps
        count(nr) @* step
    });

    (cycles, time) = iterate_until(mintime, { c =>
        SparseCompRow.matmult(N, val, rows, cols, x, c);
    });

    flops(SparseCompRow.num_flops, (N, nz, cycles), 1, time);
};

measureLU(N, mintime, R) {
    A = repeat(N, { repeat(N, R) });

    (cycles, time) = repeat_until(mintime, {
        LU.factor(N, N, A); ()
    });

    // TODO: verify solution
    flops(LU.num_flops, N, cycles, time);
};
