package Lab4.graph;

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Helpers {
    /**
     * Recover the shortest path from a source node to a target node
     * from the predecessor map returned from a BFS.
     *
     * There must be a valid path contained in the predecessor map.
     *
     * @param source name of the source node.
     * @param target name of the target node.
     * @param pred predecessor map.
     * @return List containing names of nodes that must be traversed
     *         (including the source node and target nodes) in order
     *         to reach the target node while transitioning through the lowest
     *         number of edges.
     * @throws IllegalArgumentException if there is no valid path contained
     *                                  in the predecessor map.
     */
    public static List<String> BFSPathExtract(String source, String target,
                                              Map<String, String> pred) {
        var l = new ArrayList<String>();

        if (!pred.containsKey(target))
            throw new IllegalArgumentException(
                    "the target node is not found in the predecessor map.");

        do {
            var p = pred.get(target);
            l.add(target);

            if (p == null) {
                throw new IllegalArgumentException(
                    "the source node is not found in the predecessor map.");
            }

            target = p;
        } while (!Objects.equal(target, source));

        l.add(target);
        Collections.reverse(l);

        return l;
    }
}
