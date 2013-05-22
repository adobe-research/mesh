
To run the test from the command-line:

> bin/shell -load benchmarks.scimark.CommandLine < /dev/null

Output will look something like this (takes about 30 seconds 
and doesn't print anything until it's done):

()
"SciMark 2.0a"
()
"Composite Score: 48.930252669425805"
"FFT (1024): 3.849401171424576"
"SOR (100x100): 108.6465381917423"
"Monte Carlo : 12.787511841909588"
"Sparse matmult (N=1000, nz=5000): 50.881987125544924"
"LU (100x100): 68.48582501650766"
> INFO <shell>: quitting, 0 total errors


