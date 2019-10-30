package Lab4.graph;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Set.of;

/**
 * Class representing a undirected and unweighted graph, with no self-edges
 * and multiple edges between the same two nodes.
 *
 * Strings are used to uniquely identify each node.
 */
public class Graph {
    private SortedMap<String, Node<String>> nodes;

    /**
     * Construct a graph from a map mapping node names to node objects
     * representing the named nodes.
     *
     * The created graph optionally uses deep-copied nodes, and in that case
     * would not be affected if mutable objects in the source map are modified.
     *
     * @param src mapping to use.
     * @param copy whether to deep-copy the input data to avoid dependencies.
     */
    public Graph(Map<String, Node<String>> src, boolean copy) {
        nodes = new TreeMap<>();

        if (!copy) {
            nodes.putAll(src);
            return;
        }

        for (var nodeName : src.keySet())
            nodes.put(nodeName, new Node<>(nodeName));

        for (var node : src.values()) {
            var copyNode = nodes.get(node.getName());

            for (var linkedNode : node.getNeighbors()) {
                var copyLinkedNode = nodes.get(linkedNode.getName());
                if (copyNode.isNeighbor(copyLinkedNode))
                    continue;

                copyNode.addNeighbor(copyLinkedNode);
            }
        }
    }

    /**
     * Construct a new graph from a reader providing a graph in CSV format.
     * @param r reader to load the graph from.
     * @param sorter sorter function that determines the order of graph
     *               traversal by determining the order of nodes within a
     *               neighbor list of a node.
     * @throws IOException on I/O error.
     */
    public Graph(Reader r,
                 Consumer<ArrayList<Node<String>>> sorter) throws IOException {
        /*
         * Temporary storage to hold nodes as they are being read.
         */
        nodes = new TreeMap<>();

        /*
         * Build nodes into temporary storage.
         */
        try (var in = CSVParser.parse(r, CSVFormat.RFC4180)) {
            for (var record : in) {
                var rnum = in.getRecordNumber();

                if (record.size() < 1)
                    throw new IllegalArgumentException(String.format(
                            "record %d: node unnamed / empty line.", rnum));

                if (record.size() < 2)
                    throw new IllegalArgumentException(String.format(
                            "record %d: node has no neighbors.", rnum));

                var nodeName = record.get(0);

                for (var n : record)
                    nodes.putIfAbsent(n, new Node<>(n));

                Node<String> node = nodes.get(nodeName);
                for (var i = 1; i < record.size(); ++i) {
                    Node<String> toLink = nodes.get(record.get(i));

                    if (node.isNeighbor(toLink))
                        continue;

                    node.addNeighbor(toLink);
                }
            }
        }

        /*
         * Define the visitation order.
         */
        nodes.forEach((k, v) -> v.rearrangeNeighbors(sorter));
    }

    /**
     * Rearrange the adjacency lists storing neighbor information in each
     * of the nodes using the provided function.
     *
     * This affects the order in which nodes are visited during traversal.
     *
     * @param rearranger function to use to rearrange the adjacency lists.
     */
    public void rearrange(Consumer<ArrayList<Node<String>>> rearranger) {
        nodes.forEach((k, v) -> v.rearrangeNeighbors(rearranger));
    }

    /**
     * Create a new (deep) copy of the graph with (the) specified node(s)
     * removed.
     *
     * @param n name of the nodes to remove.
     * @return a new copy of the graph with node(s) removed.
     * @throws IllegalArgumentException when there exist(s) no node(s) with the
     *                                  given name(s).
     */
    public Graph remove(String... n) {
        var namesToRemove = Set.of(n);
        if(!namesToRemove.stream()
                .allMatch(name -> nodes.containsKey(name)))
            throw new IllegalArgumentException("one or more nodes specified" +
                    "are not contained in the graph.");

        var newNodes = new TreeMap<String, Node<String>>();
        nodes.keySet().stream()
                .filter(name -> !namesToRemove.contains(name))
                .forEach(name -> newNodes.put(name, new Node<String>(name)));

        for (var newNodeName : newNodes.keySet()) {
            var oldNode = nodes.get(newNodeName);
            var newNode = newNodes.get(newNodeName);

            oldNode.getNeighbors().stream()
                    .filter(neigh -> !namesToRemove.contains(neigh.getName()))
                    .forEach(neigh -> {
                        var oldLinkedNodeName = neigh.getName();
                        var newLinkedNode = newNodes.get(oldLinkedNodeName);
                        if (!newNode.isNeighbor(newLinkedNode))
                            newNode.addNeighbor(newLinkedNode);
                    });
        }

        return new Graph(newNodes, false);
    }

    /**
     * Performs a breath first search on the graph, attempting the locate
     * a node, and recovering the path to that node.
     *
     * All collections passed to this method must have been cleared.
     *
     * If the target node cannot be successfully located, there is no entry
     * for that node in the map.
     *
     * The collections are not allocated internally in order to isolate
     * the cost of creating these collections from the search time.
     *
     * Of course, the implementation backing these collection interfaces
     * would change the performance of the search, but this is additional
     * information the method does not know. Benchmarkers must take care to keep
     * the implementations of these data structures unchanged to make
     * sound performance comparisons.
     *
     * @param s name of source node to start BFS from.
     * @param t name of target node to find.
     * @param pred map used to store node predecessor information.
     * @param visited set used to mark nodes as visited.
     * @param queue queue used to store nodes pending visitation.
     * @throws IllegalArgumentException if the source or target node cannot
     *                                  be found in the graph.
     */
    public void breadthFirstSearch(String s, String t,
                                   Map<String, String> pred,
                                   Set<String> visited,
                                   Deque<Node<String>> queue) {

        if ((!nodes.containsKey(s)) || (!nodes.containsKey(t)))
            throw new IllegalArgumentException(
                    "source / target node not contained in graph.");

        queue.add(nodes.get(s));
        while (!queue.isEmpty()) {
            var n = queue.removeFirst();
            visited.add(n.getName());

            for (var neigh : n.getNeighbors()) {
                if (visited.contains(neigh.getName()))
                    continue;

                queue.add(neigh);
                pred.put(neigh.getName(), n.getName());
            }
        }
    }

    @Override
    public String toString() {
        var builder = new StringBuilder(String.format("Graph with nodes:%n"));

        nodes.forEach((k, v) -> builder.append(String.format("%s%n",
                v.toString())));

        return builder.toString();
    }
}
