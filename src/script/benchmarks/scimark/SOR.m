
import * from std;
export .;

num_flops(M, N, num_iterations) {
    i2f( (M-1) * (N-1) * num_iterations * 6)
};

execute(M, N, omega, G, num_iterations) {
    cyclen(G, num_iterations, { g =>
        [ g[0] ] + 
        range(1,M-2) | { i =>
            [ g[i][0] ] + 
            range(1,N-2) | { j =>
                (omega /. 4.0)  *. (g[i-1][j] +. g[i+1][j] +. g[i][j-1] +. g[i][j+1]) +.
                (1.0 -. omega) *. g[i][j]
            } +
            [ g[i][N-1] ]
        } +
        [ g[M-1] ]
    })
};
