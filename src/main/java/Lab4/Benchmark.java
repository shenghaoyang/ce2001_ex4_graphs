package Lab4;

import Lab4.graph.Graph;
import Lab4.graph.Helpers;
import Lab4.graph.Node;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;

@CommandLine.Command(description = "Benchmark the performance of BFS.",
                     name = "bfsBench", mixinStandardHelpOptions = true,
                     version = "0")
class BenchmarkCommand implements Callable<Integer> {
    private enum SizeParameter {
        EDGES,
        NODES
    };

    @CommandLine.Option(names = "--size", required = true,
            description = "Size value used in the benchmark ")
    private int size;

    @CommandLine.Option(names = "--no-preserve-path", negatable = true,
            defaultValue = "false",
            description = "Ensure that there is always a path " +
                    "between the start city and end city for " +
                    "each draw. Defaults to true.")
    private boolean preservePath;

    @CommandLine.Option(names = "--loops-per-draw", required = true,
            description = "Number of loops to run for each" +
                    " draw size.")
    private int loopsPerDraw;

    @CommandLine.Option(names = "--draws", required = true,
            description = "Number of random draws to perform.")
    private int draws;

    @CommandLine.Option(names = "--graph", required = true,
            description = "Path to file containing graph data in " +
                    "RFC 4810 format. Assumbed to be in UTF-8 encoding.")
    File graphSrc;

    @CommandLine.Parameters(index = "0",
            description = "What size attribute of the graph " +
            "to alter. Alterations are done " +
            "randomly. Valid values: ${COMPLETION-CANDIDATES}")
    private SizeParameter sizeParameter;

    @CommandLine.Parameters(index = "1",
            description = "Cities to start and end search at",
            arity = "2")
    private String[] cities;

    @Override
    public Integer call() {
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


        var visited = new HashSet<String>();
        var pred = new HashMap<String, String>();
        var toVisit = new ArrayDeque<Node<String>>();

        try {
            citiesGraph.breadthFirstSearch(cities[0], cities[1],
                    pred, visited, toVisit);
        } catch (IllegalArgumentException e) {
            System.err.printf("Error: city name not found in graph: %s%n",
                    e.getLocalizedMessage());
            return 1;
        }

        var path = Helpers.BFSPathExtract(cities[0], cities[1], pred);
        var joiner = Joiner.on(" -> ");
        System.out.printf("BFS path: %s%n", joiner.join(path));

        return 0;
    }
}
