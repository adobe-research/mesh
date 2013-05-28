
import * from std;
export .;

num_flops(M, N, num_iterations) {
    i2f( (M-1) * (N-1) * num_iterations * 6)
};

execute(M, N, omega, G, num_iterations) {

    omegaDiv4 = omega /. 4.0;
    oneMinusOmega = 1.0 -. omega;
    firstRow = [ G[0] ];
    lastRow = [ G[M - 1] ];

    cyclen(G, num_iterations, { g =>
        firstRow +
        range(1,M-2) | { i =>
            [ g[i][0] ] +
            range(1,N-2) | { j =>
                omegaDiv4  *. (g[i-1][j] +. g[i+1][j] +. g[i][j-1] +. g[i][j+1]) +.
                oneMinusOmega *. g[i][j]
            } +
            [ g[i][N-1] ]
        } +
        lastRow
    })
};
