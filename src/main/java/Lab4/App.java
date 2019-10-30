package Lab4;

import Lab4.graph.Graph;
import Lab4.graph.Node;
import Lab4.graph.Helpers;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;

public class App {
    public static void main(String[] args) {
        var nodedef = "Singapore,Perth\r\nPerth,Greenland,Iceland\r\nIceland,Greenland";
        var in = new StringReader(nodedef);

        try {
            var graph = new Graph(in, v -> {});
            System.out.printf("Graph read:%n%s", graph);

            var visited = new HashSet<String>();
            var pred = new HashMap<String, String>();
            var toVisit = new ArrayDeque<Node<String>>();

            graph.breadthFirstSearch("Singapore", "Greenland",
                    pred, visited, toVisit);

            var path = Helpers.BFSPathExtract("Singapore", "Greenland",
                    pred);

            var joiner = Joiner.on("->");
            System.out.printf("BFS path: %s%n", joiner.join(path));

        } catch (IOException e) {
            System.err.printf("Error reading from input graph CSV: %s%n",
                    e.getLocalizedMessage());
            System.exit(1);
        }

    }
}
