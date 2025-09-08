Wide and short 78 27
Benchmark                    Mode  Cnt      Score      Error  Units
MLBench.reduceMatrixScalar   avgt    6  13061.536 ± 1525.797  ns/op
MLBench.reduceMatrixVUnmask  avgt    6   5364.264 ±  537.798  ns/op
MLBench.reduceMatrixVector   avgt    6   8668.008 ±  637.307  ns/op
MLBench.reduceMatrixWP       avgt    6  13745.004 ± 1676.414  ns/op

Narrow and Tall
Benchmark                    Mode  Cnt      Score      Error  Units
MLBench.reduceMatrixScalar   avgt    6  19595.644 ± 1915.696  ns/op
MLBench.reduceMatrixVUnmask  avgt    6  12341.488 ±  403.551  ns/op
MLBench.reduceMatrixVector   avgt    6  13787.198 ± 1325.215  ns/op
MLBench.reduceMatrixWP       avgt    6  18918.499 ±  442.462  ns/op

7x7
Benchmark                    Mode  Cnt    Score    Error  Units
MLBench.reduceMatrixScalar   avgt    6  204.918 ± 19.457  ns/op
MLBench.reduceMatrixVUnmask  avgt    6  239.056 ± 22.700  ns/op
MLBench.reduceMatrixVector   avgt    6  267.608 ±  6.221  ns/op
MLBench.reduceMatrixWP       avgt    6  221.332 ± 16.922  ns/op

8x8
Benchmark                    Mode  Cnt    Score    Error  Units
MLBench.reduceMatrixScalar   avgt    6  219.061 ± 26.899  ns/op
MLBench.reduceMatrixVUnmask  avgt    6  263.450 ± 26.440  ns/op
MLBench.reduceMatrixVector   avgt    6  302.809 ± 13.630  ns/op
MLBench.reduceMatrixWP       avgt    6  227.757 ± 19.310  ns/op

Small Wide (8, 15)
Benchmark                    Mode  Cnt    Score    Error  Units
MLBench.reduceMatrixScalar   avgt    6  305.563 ± 39.626  ns/op
MLBench.reduceMatrixVUnmask  avgt    6  267.676 ± 19.283  ns/op
MLBench.reduceMatrixVector   avgt    6  378.768 ± 83.826  ns/op
MLBench.reduceMatrixWP       avgt    6  344.072 ± 29.626  ns/op

Small Narrow
Benchmark                    Mode  Cnt    Score    Error  Units
MLBench.reduceMatrixScalar   avgt    6  498.552 ± 82.593  ns/op
MLBench.reduceMatrixVUnmask  avgt    6  622.071 ± 79.770  ns/op
MLBench.reduceMatrixVector   avgt    6  782.735 ± 76.505  ns/op
MLBench.reduceMatrixWP       avgt    6  530.841 ± 56.619  ns/op

Huge Narrow (173, 53)
Benchmark                    Mode  Cnt       Score       Error  Units
MLBench.reduceMatrixScalar   avgt    6  136941.969 ± 25074.131  ns/op
MLBench.reduceMatrixVUnmask  avgt    6   62939.361 ±  5564.523  ns/op
MLBench.reduceMatrixVector   avgt    6   98104.899 ± 10935.929  ns/op
MLBench.reduceMatrixWP       avgt    6  133872.205 ± 20693.966  ns/op

Executed on an i5-1240p (so AVX2, nothing higher)
Honestly no idea *how* good these are, but the general trend seems to be that for matrices of reasonable size Graal
doesn't autovectorize and the unmasked version performs best. Wikipedian performs better for smaller matrices, but
concerningly scalar has an advantage in some cases. I'm not sure what to think of that.
