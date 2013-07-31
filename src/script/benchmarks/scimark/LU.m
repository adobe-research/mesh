/**
    LU matrix factorization. (Based on TNT implementation.)
    Decomposes a matrix A  into a triangular lower triangular
    factor (L) and an upper triangular factor (U) such that
    A = L*U.  By convnetion, the main diagonal of L consists
    of 1's so that L and U can be stored compactly in
    a NxN matrix.
*/

export num_flops,factor;

num_flops(N) {
    // roughly 2/3 * N^3
    i2f(2 * pow(N, 3)) /. 3.0
};

// Returns combined matrix and pivot values
// TODO: this is implemented using a recursive algorithm, which will
// end up popping the stack with a big enough array.  We should 
// try this iteratively.
factor_recursive(M:Int, N:Int, A:[[Double]]) -> ([[Double]], [Int]) {
    guard(M == 1 || { N == 1 }, (A, [0] ), { 
        // Find the row with the largest abs max value in the first column.  
        col0 = A | head;
        pivot_pos = reduce(
            { iif(fabs(col0[$1]) >. fabs(col0[$0]), $1, $0) }, 
            0, fromto(1, M-1));

        // extract pivot row 
        pivot_value = head(A[pivot_pos]);
        pivot_row = tail(A[pivot_pos]);
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
        solution = [ [ pivot_value ] + pivot_row ] + 
            (zip(pivot_col, solvedsub) | { [ $0 ] + $1 });

        // adjust subpivot array depth
        pivots =  [ pivot_pos ] + (subpivots | inc);

        (solution, pivots)
    })
};


// Perform the factorization in place using boxes.  
factor_iterative(M:Int, N:Int, A:[[Double]]) -> ([[Double]], [Int]) {
    R = A | { box($0 | { box($0) }) };
    CT = min(M,N);

    getvalue(i,j) { get(get(R[i])[j]); };
    putvalue(i,j, x) { put(get(R[i])[j], x); };

    calc(i, j) {
        (_, __, v) = cycle(
            ( i-1, j-1, getvalue(i, j) ),
            { r,c,_ => r >= 0 && { c >= 0 } },
            { r,c,v => ( r-1, c-1, v -. (getvalue(r, j) *. getvalue(i, c)) ) });
        guard(i <= j, v, { v /. getvalue(j, j) })
    };

    pivot(r) {
        (_,  p, __) = cyclen(( -1.0, r, 0 ), CT - r,
            { max_value, max_index, i =>
              value = getvalue(r + i, r);
              if (value >. max_value, 
                  { (value, r + i, i + 1 ) },
                  { (max_value, max_index, i + 1 ) })
                
            });
        tmprow = *(R[r]);
        R[r] := *(R[p]);
        R[p] := tmprow;

        p
    };

    pivots = count(CT) | { r => 
        p = pivot(r);
        count(N) | { c => putvalue(r, c, calc(r, c)); };
        p
    };

    ( R | { get($0) } | { $0 | get }, pivots )
};

// change this to iterative to try that out.  Iterative version is approx. 3 x slower at the moment
factor = factor_recursive;
