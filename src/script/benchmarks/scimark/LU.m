/**
    LU matrix factorization. (Based on TNT implementation.)
    Decomposes a matrix A  into a triangular lower triangular
    factor (L) and an upper triangular factor (U) such that
    A = L*U.  By convnetion, the main diagonal of L consists
    of 1's so that L and U can be stored compactly in
    a NxN matrix.
*/

import * from std;

LU() { 
    num_flops(N) {
        // roughly 2/3 * N^3
        i2f(2 * pow(N, 3)) /. 3.0
    };
    
    // Returns combined matrix and pivot values
    // TODO: this is implemented using a recursive algorithm, which will
    // end up popping the stack with a big enough array.  We should 
    // try this iteratively.
    factor(M:Int, N:Int, A:[[Double]]) -> ([[Double]], [Int]) {
        guard(M == 1 || { N == 1 }, (A, [0] ), { 
            // Find the row with the largest abs max value in the first column.  
            col0 = A | head;
            pivot_pos = reduce(
                { iif(fabs(col0[$1]) >. fabs(col0[$0]), $1, $0) }, 
                0, fromto(1, M-1));
    
            // extract pivot row 
            pivot_value = head(A[pivot_pos]);
            pivot_row = rest(A[pivot_pos]);
            A_nopivtorow = if(pivot_pos == M-1, { drop(-1, A) }, 
                { mapll(count(pivot_pos) + fromto(pivot_pos + 1, M-1), A) });
    
            // TODO: check for pivot_value == 0.  If so we have a signularity and 
            // cannot continue.
    
            // extract first column, scaling by the reciprical of the pivot value
            // to normalize diagonal to 1
            reciprical = 1.0 /. pivot_value;
            pivot_col = A_nopivtorow | { head($0) *. reciprical };
            A_nopivot = A_nopivtorow | { drop(1, $0) };
    
            // Reduce submatrix values by the product of corresponding pivot
            // row/col values
            submatrix = zip(A_nopivot, pivot_row) | 
                { row, rowfactor => zip(row, pivot_col) | 
                    { value, colfactor => value -. (rowfactor *. colfactor) } };
    
            // solve the submatrix
            results:([[Double]], [Int]) = factor(M-1, N-1, submatrix);
            solvedsub = results.0;
            subpivots = results.1;
    
            // Now put it all back together again
            solution = enlist( enlist(pivot_value) + pivot_row ) + 
                (zip(pivot_col, solvedsub) | { enlist($0) + $1 });
    
            // adjust subpivot array depth
            pivots = enlist(pivot_pos) + (subpivots | inc);
    
            (solution, pivots)
        })
    };
    
    ( #num_flops: num_flops, #factor: factor )
};
