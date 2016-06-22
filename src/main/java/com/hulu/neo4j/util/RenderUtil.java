package com.hulu.neo4j.util;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

/**
 * Created by simei.he on 6/22/16.
 */
public class RenderUtil {

    public static String toString(Node n) {
        StringBuilder result = new StringBuilder();

        result.append("("+n.getId()+":");
        if(n.getLabels() != null) {
            for(Label l : n.getLabels()) {
                result.append(l.name());
                result.append(",");
            }
        }
        if(n.getAllProperties() != null) {
            result.append(n.getAllProperties());
        }
        result.append(")");
        return result.toString();
    }

    // mind the relationship direction
    public static String toString(Relationship r) {
        StringBuilder result = new StringBuilder();
        result.append("-[" + r.getId() + ":" + r.getType());
//        if(r.getAllProperties() != null) {
//            result.append(r.getAllProperties());
//        }
        result.append("]-");
        return result.toString();
    }

    public static String render(org.neo4j.graphdb.Path path) {
        StringBuilder result = new StringBuilder();
        for (PropertyContainer pc : path) {
            if (pc instanceof Node) {
                result.append(toString((Node) pc));
            } else {
                result.append(toString((Relationship) pc));
            }
        }
        return result.toString();
    }
}
