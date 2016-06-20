package com.hulu.neo4j;

import org.apache.lucene.search.Weight;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphalgo.WeightedPath;


import java.util.LinkedList;
import java.util.List;

/**
 * Created by simei.he on 6/14/16.
 */

public class WeightedPathImpl {
    private Path path;
    private double weight;
    private String prop;

    public WeightedPathImpl(Path path, String prop) {
        this.path = path;
        this.prop = prop;
        this.weight = calculateWeight();
    }

    public double calculateWeight() {
        double result = 0.0;
        for(Relationship r: path.relationships()) {
            Object valueOrNull = r.getProperty(prop, null);
            if(valueOrNull != null) {
                result += (Double) valueOrNull;
            }
        }
        return result;
    }
    public double getWeight() {
        return this.weight;
    }
    public Path getPath() {
        return this.path;
    }
    public String getProp() {
        return this.prop;
    }

    public String render() {
        StringBuilder result = new StringBuilder();
        result.append(prop + ":" + weight+ ",");
        result.append(PathResource.render(this.path));
        return result.toString();
    }


}

