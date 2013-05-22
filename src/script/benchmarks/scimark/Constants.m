
RESOLUTION_DEFAULT = 2.0; /*secs*/
RANDOM_SEED = 101010;
    
// default: small (cache-contained) problem sizes
//
FFT_SIZE = 1024;  // must be a power of two
SOR_SIZE = 100; // NxN grid
SPARSE_SIZE_M = 1000;
SPARSE_SIZE_nz = 5000;
LU_SIZE = 100;

// large (out-of-cache) problem sizes
//
LG_FFT_SIZE = 1048576;  // must be a power of two
LG_SOR_SIZE = 1000; // NxN grid
LG_SPARSE_SIZE_M = 100000;
LG_SPARSE_SIZE_nz = 1000000;
LG_LU_SIZE = 1000;

// tiny problem sizes (used to mainly to preload network classes
//                     for applet, so that network download times
//                     are factored out of benchmark.)
//
TINY_FFT_SIZE = 16;  // must be a power of two
TINY_SOR_SIZE = 10; // NxN grid
TINY_SPARSE_SIZE_M = 10;
TINY_SPARSE_SIZE_N = 10;
TINY_SPARSE_SIZE_nz = 50;
TINY_LU_SIZE = 10;

SCIMARK_DEBUG = false
