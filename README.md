# ce2001_ex4_graphs

Benchmarking setup for BFS in a undirected, unweighted graph.

This implementation makes use of Google's guava Java library in order to
perform result validation and analysis.

Implemented in order to tackle requirements for Example class 4 in NTU's 
CE2002 course (AY19/20 Semester 1).

Gradle is used to manage the build.

## What's working

- Graph class

    - Reading a graph from its CSV representation.
    - Performing a BFS with given target and start nodes to find the
      shortest path between two nodes.
    - Deleting nodes from graphs in a non in-place manner for performance
      comparisons with different graph sizes.

## What's to be done

- Graph class
    - Exporting the graph to a Writer for visualization in the presentation.

- Main application
    - [x] Benchmark harness to test BFS.

- Benchmark setup
    - JVM options for benchmarking.
        - [x] AOT compile (GraalVM)
        - [x] Disable JIT
        - [x] Use low-jitter GC (Shenandoah)
        - ..... any other things that we can do to make our performance
          measurements more consistent.

- Tests
    - Test cases validating BFS correctness (could compare against guava).
    - Test cases verifying saving / restoring graphs from CSV.
    - Test cases verifying node deletion functionality.
    
## The data

- The data file is located at `data/connected_airports.csv`

- The input data we will be performing BFS on is a undirected, unweighted,
  graph with no self-edges where the nodes are airports and the edges represent 
  bidirectional direct connections between airports (i.e, nodes that have an 
  edge between them represent airports (A, B) that have at least one flight
  from A -> B  AND also another flight from B -> A, where the flights are
  both non-stop).

- The input data is stored in CSV format, with each row containing information
  on one node, with the first column in each row representing
  the airport name, and subsequent columns representing the names of 
  airports that this airport is connected to. Airports are referred to using
  their IATA codes. (i.e, the adjacency list of that node is represented by 
  the subsequent columns).
  
- Each edge (A <-> B) is specified twice, once in the row representing the
  airport A and another time in the row representing the airport B.
  
## Testing methodology

- We are requested to benchmark the performance of BFS in terms of CPU time,
  against various input graph sizes. Here, we'll vary the number of nodes.

- We vary the graph size by randomly choosing nodes to eliminate from the graph
  in order to build a graph of smaller size. This process is called a _draw_,
  and _draws_ are performed for a pre specified number of times.
  
- After each _draw_ to produce a smaller graph, we run BFS to locate the
  shortest path between two airports in terms of plane changes, and measuring
  the time taken. The actual implementation measures the average time for BFS
  after looping through the BFS search a number of times.

- In each timing loop, we mutate the adjacency lists of the nodes in the graph
  produced from the _draw_ process in a specified manner, either sorting it
  in an ascending fashion, a descending fashion, or a random manner.

- After the timing loops, the average edge count and BFS search time are 
  reported and collected.

- After all _draws_ complete, the average edge count for all draws and the
  average BFS search time for all draws (representing an average search time 
  for a graph of this number of nodes) are reported.
  
- The appropriate metrics are processed and analysis results are produced.

## Building the benchmark tool

### Clone from git

```
git clone https://github.com/shenghaoyang/ce2001_ex4_graphs.git
cd ce2001_ex4_graphs
./gradlew build
./gradlew run # Runs the benchmark application using the JVM with JIT 
              # and the default GC
```

After cloning, you can also import the project into your favorite IDE,
most of which should support Gradle.

## Running the benchmark tool

The benchmark tool accepts the following options, which can be passed
using the `--args='<arg1> <arg2>'` parameter when invoking runs
using the JVM.

```
Usage: bfsBench [-hV] [--[no-]preserve-path] --draws=<draws> --graph=<graphSrc>
                   --loops-per-draw=<loopsPerDraw> --size=<size>
                   [--sort-order=<sortMode>]
                   [--warmup-loops-per-draw=<warmupLoopsPerDraw>] <airports>
                   <airports>
   Benchmark the performance of BFS.
         <airports> <airports>  Airports (represented by their codes) to start and
                                  end search at
         --draws=<draws>        Number of random draws to perform.
         --graph=<graphSrc>     Path to file containing graph data in RFC 4810
                                  format. Assumbed to be in UTF-8 encoding.
     -h, --help                 Show this help message and exit.
         --loops-per-draw=<loopsPerDraw>
                                Number of loops to run for each draw size.
         --[no-]preserve-path   Ensure that there is always a path between the
                                  start airport and end airport for each draw.
                                  Defaults to true.
         --size=<size>          Number of nodes to use in the benchmark
         --sort-order=<sortMode>
                                How to sort the adjacency lists of each node.
                                  Valid values: ASCENDING, DESCENDING, RANDOM
     -V, --version              Print version information and exit.
         --warmup-loops-per-draw=<warmupLoopsPerDraw>
                                Number of warm up loops to do per random draw
                                  before accumulating actual results
```

- `draws` specifies the number of random (node) draws to perform to obtain
  a graph of node size as specified in `size`.

- `graph` specifies the input file to the large input dataset from which
  the dataset of size `size` will be generated. An input dataset is
  provided under `data/connected_airports.csv`.

- `loops-per-draw` specifies the amount of loops to run per random node draw
  when benchmarking the time it takes to perform BFS.

- `preserve-path` OR `no-preserve-path` specifies whether to ensure that there
  is always a path between the destination and source airport in a generated
  graph of size `size` before performing BFS on that graph. Useful for ensuring
  that the search does not degenerate into a full BFS exploration of the graph.

- `size` specifies the size of the graph (in number of nodes) to benchmark 
  BFS on. Must be lower or equal to the size of the graph specified in the
  input file.
  
- `sort-order` specifies the order in which the adjacency lists of each node
  in the generated graph of size `size` will be arranged, per per BFS timing
  loop. Useful for ensuring that we do not prefer a specific route between
  the source and destination nodes.
  
- `warmup-loops-per-draw` specifies the additional number of loops to run 
  for each draw to serve as additional computation to warm up the JVM
  and force it to load classes and perform JIT.

### Using the GraalVM native image (Recommended - most consistent performance)

Running the GraalVM native image allows for the most consistent performance
as all compilation is done AOT. It also lets us complete the benchmark in
a reasonable amount of time as compared to disabling JIT in the JVM, since
we are executing native machine code.

```
./gradlew nativeImage # This will take a while, GraalVM needs to be downloaded
build/graal/Lab4 <arguments>
```

### Using the JVM with JIT using the default GC.

This is not recommended due to GC STW pauses which may affect measurement
results, and also needs careful tuning of the warm-up loops to ensure that
the measurement loops run with consistent timings. 

```
./gradlew run --args='<arguments>'
```

### Using the JVM without JIT using the Shenandoah low-latency GC.

This uses the low latency GC introduced in OpenJDK 12, which runs concurrently
with the with the running Java program to produce lower STW times.

It also disables the JIT so that measurement loops run with more consistent
timings, but it may be time-consuming to complete large-sized benchmarks.

You might need to alter the task definition in `build.gradle` to set 
memory-related options specific to your platform.

```
./gradlew benchmarkNoJitShenandoah --args='<arguments>'
```

### Using the JVM with the JIT using the Shenandoah low-latency GC.

This uses the low latency GC introduced in OpenJDK 12, which runs concurrently
with the with the running Java program to produce lower STW times.

The JIT is enabled, however, so care must be taken when defining the warm-up
loop iterations to ensure that the measurement loops have consistent timings.

You might need to alter the task definition in `build.gradle` to set 
memory-related options specific to your platform.

```
./gradlew benchmarkJitShenandoah --args='<arguments>'
``` 

## Benchmark results

See results in the `results/` folder.
  
## LICENSE

See `LICENSE` for more details.

This license does not apply to the data files, which are covered under
their own terms as described below.

## Data Attribution

`data/connected_airports.csv` was generated using data from 
[openflights.org](https://www.openflights.org). Its source file is located
at `data/routes.dat` and the Python script used to generate that is located
at `utils/extractairportconnections.py`.

Both datasets / databases are available under the Open Database License,
and any rights in individual contents of the database are licensed under 
the Database Contents License.

Copies of both of these licenses can be found in the file `data/LICENSE`.
