package Lab4.Benchmark;

import Lab4.graph.Graph;
import Lab4.graph.Helpers;
import Lab4.graph.Node;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

@CommandLine.Command(description = "Benchmark the performance of BFS.",
                     name = "bfsBench", mixinStandardHelpOptions = true,
                     version = "0")
public class BenchmarkCommand implements Callable<Integer> {
    /**
     * Methods to sort the adjacency lists of each node.
     */
    private enum SortMode {
        /**
         * Sort the adjacency lists in ascending (alphabetical) order.
         */
        ASCENDING,
        /**
         * Sort the adjacency lists in descending (alphabetical) order.
         */
        DESCENDING,
        /**
         * Sort the adjacency lists in random order.
         */
        RANDOM
    }

    @CommandLine.Option(names = "--size", required = true,
            description = "Number of nodes to use in the benchmark ")
    private int size;

    @CommandLine.Option(names = "--no-preserve-path", negatable = true,
            defaultValue = "true",
            description = "Ensure that there is always a path " +
                    "between the start city and end city for " +
                    "each draw. Defaults to true.")
    private boolean preservePath;

    @CommandLine.Option(names = "--loops-per-draw", required = true,
            description = "Number of loops to run for each" +
                    " draw size.")
    private int loopsPerDraw;

    @CommandLine.Option(names = "--warmup-loops-per-draw", required = false,
            description = "Number of warm up loops to do per random " +
                    "draw before accumulating actual results",
            defaultValue = "20")
    private int warmupLoopsPerDraw;

    @CommandLine.Option(names = "--draws", required = true,
            description = "Number of random draws to perform.")
    private int draws;

    @CommandLine.Option(names = "--graph", required = true,
            description = "Path to file containing graph data in " +
                    "RFC 4810 format. Assumbed to be in UTF-8 encoding.")
    private File graphSrc;

    @CommandLine.Option(names = "--sort-order", required = false,
                        defaultValue = "RANDOM", description = "How to sort " +
            "the adjacency lists of each node. " +
            "Valid values: ${COMPLETION-CANDIDATES}")
    private SortMode sortMode;

    @CommandLine.Parameters(index = "0",
            description = "Cities to start and end search at",
            arity = "2")
    private String[] cities;

    /**
     * Randomly select unique elements from an array of strings.
     *
     * @param from array to select elements from.
     * @param count count of elements to select.
     * @param rnd random source to use.
     * @return array containing unique elements selected.
     */
    static String[] Select(String[] from, int count, Random rnd) {
        return rnd.ints(0, from.length)
                .distinct()
                .limit(count)
                .mapToObj(i -> from[i])
                .toArray(String[]::new);
    }

    @Override
    public Integer call() {
        if (draws <= 0) {
            System.err.println("Error: number of draws must be positive.");
            return 1;
        }

        if (loopsPerDraw <= 0) {
            System.err.println("Error: loops per draw must be positive.");
            return 1;
        }

        if (warmupLoopsPerDraw < 0) {
            System.err.println("Error: warmup loops per draw must " +
                    "be non-negative.");
            return 1;
        }

        Graph citiesGraph;
        var source = Files.asCharSource(graphSrc, StandardCharsets.UTF_8);

        try {
            var r = source.openBufferedStream();
            citiesGraph = new Graph(r, x -> {});
        } catch (IOException e) {
            System.err.printf("Error: cannot read graph from file: %s%n",
                    e.getLocalizedMessage());
            return 1;
        }

        var origNodes = citiesGraph.getNodeCount();
        var origEdges = citiesGraph.getEdgeCount();
        System.out.printf("Loaded graph with %d nodes and %d edges.%n",
                origNodes, origEdges);

        if ((origNodes - size) < 0) {
            System.err.printf("Error: target graph node count is larger than " +
                    "source graph node count.%n");
            return 1;
        }

        System.out.printf("Benchmarking using a graph size of %d node(s).%n" +
                "Using %d draw(s) with %d loop(s) per draw " +
                "(and %d warmup loops per draw).%nUsing adjacency list " +
                "sort mode %s.%n%s a path between the" +
                " destination node and source node every draw.%n",
                size, draws, loopsPerDraw, warmupLoopsPerDraw,
                sortMode, preservePath ? "Ensuring" : "Not ensuring");

        var pred = new HashMap<String, String>();
        var toVisit = new ArrayDeque<Node<String>>();

        try {
            citiesGraph.breadthFirstSearch(cities[0], cities[1], pred, toVisit);
        } catch (IllegalArgumentException e) {
            System.err.printf("Error: city name not found in graph: %s%n",
                    e.getLocalizedMessage());
            return 1;
        }


        try {
            System.out.printf("Path found using file adjacency list ordering:" +
                    " %s.%n",
                    Joiner.on(" -> ").join(
                        Helpers.BFSPathExtract(cities[0], cities[1], pred)));
        } catch (IllegalArgumentException e) {
            System.err.printf("Error: no valid path contained " +
                    "in predecessor map between %s and %s%n.",
                    cities[0], cities[1]);
            return 1;
        }

        /*
         * In each draw, select the nodes to remove from the graph in order
         * to preserve
         */
        var timePerDraw = new double[draws];
        var edgesPerDraw = new double[draws];
        var timePerLoop = new long[loopsPerDraw];
        var edgesPerLoop = new long[loopsPerDraw];
        var rng = new Random();
        for (int draw = 0; draw < draws; ++draw) {
            var mutatedGraph = citiesGraph.remove(
                    Select(citiesGraph.getNames().toArray(new String[0]),
                            origNodes - size, rng));
            var nodeNames = mutatedGraph.getNames();
            if (!(nodeNames.contains(cities[0]))
                    || !(nodeNames.contains(cities[1]))) {
                System.err.println("Warning: modified graph does " +
                        "not contain source and destination nodes. Retrying.");
                draw -= 1;
                continue;
            }

            var pathExists = true;
            var edges = mutatedGraph.getEdgeCount();
            try {
                pred.clear();
                toVisit.clear();
                mutatedGraph.breadthFirstSearch(cities[0], cities[1], pred,
                        toVisit);
                Helpers.BFSPathExtract(cities[0], cities[1], pred);
            } catch (IllegalArgumentException e) {
                if (preservePath) {
                    System.err.println("Warning: unable to find a path " +
                            "between destination and source nodes. Retrying.");
                    draw -= 1;
                    continue;
                }
                pathExists = false;
            }
            for (int loop = 0; loop < (loopsPerDraw + warmupLoopsPerDraw);
                 ++loop) {
                /*
                 * Sort the adjacency lists in the required order.
                 *
                 * Note that optimizations could be used to avoid ascending /
                 * descending sort repetitions, but it doesn't really matter.
                 */
                switch (sortMode) {
                    case RANDOM:
                        mutatedGraph.rearrange(l ->
                                Helpers.RandomRearranger(l, rng));
                        break;
                    case ASCENDING:
                        mutatedGraph.rearrange(Helpers::AscendingRearranger);
                        break;
                    case DESCENDING:
                        mutatedGraph.rearrange(Helpers::DescendingRearranger);
                }
                pred.clear();
                toVisit.clear();

                var start = System.nanoTime();
                mutatedGraph.breadthFirstSearch(cities[0], cities[1],
                        pred, toVisit);
                var end = System.nanoTime();
                var elapsed = (end - start);

                System.out.printf("Draw %d: loop %d: %d edges: %d ns: " +
                                "path found: %s.%n", draw, loop, edges,
                        elapsed, pathExists ? Joiner.on(" -> ").join(
                                Helpers.BFSPathExtract(cities[0], cities[1],
                                        pred)) : "no path");

                if (loop < warmupLoopsPerDraw)
                    continue;

                timePerLoop[loop - warmupLoopsPerDraw] = elapsed;
                edgesPerLoop[loop - warmupLoopsPerDraw] = edges;
            }

            timePerDraw[draw] = LongStream.of(timePerLoop)
                                    .average()
                                    .getAsDouble();
            edgesPerDraw[draw] = LongStream.of(edgesPerLoop)
                                    .average()
                                    .getAsDouble();
            System.out.printf("Draw %d: average edge count: %f: " +
                            "average search time: %f ns.%n",
                    draw, edgesPerDraw[draw], timePerDraw[draw]);
        }

        System.out.printf("Overall: node count: %d, average edge count: %f: " +
                        "average search time: %f ns.%n",
                size,
                DoubleStream.of(edgesPerDraw).average().getAsDouble(),
                DoubleStream.of(timePerDraw).average().getAsDouble());

        return 0;
    }
}
