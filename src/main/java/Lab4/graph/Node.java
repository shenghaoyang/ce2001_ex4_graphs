package Lab4.graph;

import java.util.*;
import java.util.function.Consumer;

/**
 * Generic graph node for an undirected and unweighted graph, with a maximum of
 * only one edge between any two nodes.
 *
 * Node that nodes of a particular type can only be linked to nodes
 * of the same type.
 *
 * @param <K> type of the data used to name the node.
 */
public class Node<K extends Comparable<K>>
        implements Comparable<Node<K>> {
    /**
     * Name of the node.
     */
    private K name;
    /**
     * Neighboring nodes connected to this node, in an ordered list
     * to enforce visitation order.
     */
    private ArrayList<Node<K>> neighbors;
    /**
     * Set containing neighboring nodes, used for quick lookups.
     */
    private Set<Node<K>> neighborSet;

    /**
     * Construct a new node with an empty neighbor list.
     *
     * @param k name of the node.
     */
    Node(K k) {
        this(k, new LinkedList<>());
    }

    /**
     * Construct a new node.
     *
     * @param k name of the node.
     * @param n list of node neighbors. The order in which nodes are presented
     *          in this list is the order of visitation any grbaph traversal
     *          operation will use when traversing a graph containing this node.
     */
    Node(K k, List<Node<K>> n) {
        name = k;
        this.neighbors = new ArrayList<>(n);
        this.neighborSet = new HashSet<>(this.neighbors);
    }

    /**
     * Yield a string representation of this node.
     *
     * The returned string will be in the form of:
     *
     * Node name: [Neighbor 0 node name, Neighbor 1 node name, ...]
     *
     * The neighbor names are written in visitation order.
     *
     * @return string representation of this node.
     */
    @Override
    public String toString() {
        var builder = new StringBuilder(
                String.format("%s: [", getName()));

        this.neighbors.forEach(n -> builder.append(
                String.format("%s, ", n.getName())));

        builder.append("]");

        return builder.toString();
    }

    /**
     * Compare this node with another node, using the name of the nodes
     * to perform the comparison.
     *
     * This method relies on the natural order of the node names.
     *
     * @param n node to compare to.
     * @return comparison result.
     */
    @Override
    public int compareTo(Node<K> n) {
        return name.compareTo(n.name);
    }

    /**
     * Obtain the name of the node.
     *
     * @return node name.
     */
    public K getName() {
        return name;
    }

    /**
     * Check if this node is a neighbor of another node.
     *
     * @param n other node.
     * @return neighbor check result.
     */
    public boolean isNeighbor(Node<K> n) {
        return neighborSet.contains(n);
    }

    /**
     * Add a node to the neighbor list.
     *
     * This node is placed at the end of the neighbor list, by default.
     *
     * If the other node does not have this node as its neighbor, then
     * this node is added to the other node's neighbor list, following the
     * above-mentioned semantics.
     *
     * This method is package-private because users should have no need to add
     * new nodes to neighbor lists of existing nodes.
     *
     * @param n node to add to the neighbor list.
     * @throws IllegalArgumentException when the node already exists
     *                                  in the list.
     */
     void addNeighbor(Node<K> n) {
        if (neighborSet.contains(n))
            throw new IllegalArgumentException(
                    String.format("Node %s is already a neighbor of node %s.",
                            n, this));

        neighbors.add(n);
        neighborSet.add(n);

        if (!n.isNeighbor(this))
            n.addNeighbor(this);
    }

    /**
     * Update the neighbor list of this node to be the one specified.
     *
     * @param l new neighbor list to use.
     */
    void updateNeighbors(List<Node<K>> l) {
        neighbors.clear();
        neighborSet.clear();
        neighbors.addAll(l);
        neighborSet.addAll(neighbors);
    }

    /**
     * Obtain the count of neighbors this node has in its adjacency list.
     *
     * @return neighbor count.
     */
    public int getNeighborCount() {
        return neighbors.size();
    }

    /**
     * Obtain a view of the neighbor list of this node.
     * Nodes are arranged in the order that they will be visited during graph
     * traversal of a graph containing this node.
     *
     * This list is immutable.
     *
     * After / during an operation to mutate the node's neighbor list,
     * this view becomes / is invalid.
     *
     * @return list of neighbor nodes.
     */
    public List<Node<K>> getNeighbors() {
        return Collections.unmodifiableList(neighbors);
    }

    /**
     * Rearrange the neighbor list using a provided function.
     *
     * This allows changes to the neighbor visitation order.
     *
     * @param arranger sorter function that determines the order of graph
     *                 traversal by determining the order of nodes within a
     *                 neighbor list of a node.
     */
    void rearrangeNeighbors(Consumer<List<Node<K>>> arranger) {
        arranger.accept(neighbors);
    }
}
