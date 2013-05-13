
import * from std;

import * from FFT;
import * from SOR;
import * from SOR;
import * from MonteCarlo;
import * from SparseCompRow;
import * from LU;

import * from Random;
import * from Stopwatch;
import Constants;

Kernel() {

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

    _measureFFT(N, mintime, R) {
        fft = FFT();
        x = repeat(2*N, R);

        (cycles, time) = repeat_until(mintime, 
            { fft.inverse(fft.transform(x)); () });

        EPS = fpow(10.0, -10.0);

        if (fft.test(x) /. i2f(N) >. EPS, { 0.0 }, 
            { flops(fft.num_flops, N, cycles, time) });
    };

    _measureSOR(N, mintime, R) {
        sor = SOR();
        G = repeat(N, { repeat(N, R) } );

        (cycles, time) = iterate_until(mintime, 
            { c => sor.execute(N, N, 1.25, G, c); () });

        if (Constants.SCIMARK_DEBUG, { print(G) }, { () });
        flops(sor.num_flops, (N, N, cycles), 1, time)
    };

    _measureMonteCarlo(mintime, R) {
        mc = MonteCarlo();
        (cycles, time) = iterate_until(mintime, { c => mc.integrate(c); });
        flops(mc.num_flops, cycles, 1, time);
    };

    _measureSparseMatmult(N, nz, mintime, R) {
        src = SparseCompRow();

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

        rows = count(N+1) | { $0 * nr };
        cols = flatten(count(N) | { r => 
            step = iif(r / nr < 1, 1, r / nr); // take at least unit steps
            count(nr) | { i => i * step }
        });

        (cycles, time) = iterate_until(mintime, { c =>
            src.matmult(N, val, rows, cols, x, c);
        });

        flops(src.num_flops, (N, nz, cycles), 1, time);
    };

    _measureLU(N, mintime, R) {
        lu = LU();
        A = repeat(N, { repeat(N, R) });

        (cycles, time) = repeat_until(mintime, {
            lu.factor(N, N, A); ()
        });

        // TODO: verify solution
        flops(lu.num_flops, N, cycles, time);
    };

    (
      #measureFFT: _measureFFT, 
      #measureSOR: _measureSOR,
      #measureMonteCarlo: _measureMonteCarlo,
      #measureSparseMatmult: _measureSparseMatmult,
      #measureLU: _measureLU
    )
};
