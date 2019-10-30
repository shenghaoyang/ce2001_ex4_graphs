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
    - Benchmark harness to test BFS.

- Benchmark setup
    - JVM options for benchmarking.
        - AOT compile?
        - Disable JIT?
        - Use low-jitter GC?
        - ..... any other things that we can do to make our performance
          measurements more consistent.

- Tests
    - Test cases validating BFS correctness (could compare against guava).
    - Test cases verifying saving / restoring graphs from CSV.
    - Test cases verifying node deletion functionality.

## Building the benchmark tool

### Clone from git

```
git clone https://github.com/shenghaoyang/ce2001_ex4_graphs.git
cd ce2001_ex4_graphs
./gradlew build
./gradlew run # Runs a basic smoke test.
```

After cloning, you can also import the project into your favorite IDE,
most of which should support Gradle.

## Benchmark results

To be done.
  
## LICENSE

See `LICENSE` for more details.
