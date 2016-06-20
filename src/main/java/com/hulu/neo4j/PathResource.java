package com.hulu.neo4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Path;
import org.neo4j.helpers.collection.Iterables;


import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

import com.google.gson.*;
/**
 * Created by simei.he on 6/14/16.
 */

@javax.ws.rs.Path("/path")
public class PathResource {
    private final GraphDatabaseService graphDb;
    private final ObjectMapper objectMapper;

    private static final RelationshipType ACTED_IN = RelationshipType.withName("ACTED_IN");

    private static final Label ACTOR = Label.label("Actor");
    private static final Label MOVIE = Label.label("Movie");

//    private final Map properties = new HashMap<>();


    public PathResource(@Context GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        this.objectMapper = new ObjectMapper();
    }

    @GET
    @javax.ws.rs.Path("/all/movie/{movie1}/movie/{movie2}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findAllPath(
            final @PathParam("movie1") String movie1,
            final @PathParam("movie2") String movie2,
            @DefaultValue("3")@QueryParam("depth") Integer depth,
            @DefaultValue("10000000")@QueryParam("limit") Integer limit,
            @DefaultValue("false")@QueryParam("simple") boolean simple,
            @DefaultValue("false")@QueryParam("sort") boolean sort) throws IOException {
        //List<org.neo4j.graphdb.Path> result = new LinkedList<>();
//        Gson gson = new Gson();
//        Gson gson = new GsonBuilder().serializeNulls().create();
        List<String> resp = new LinkedList<>();
        List<WeightedPathImpl> weightedPaths = new LinkedList<>();
        try (Transaction tx = graphDb.beginTx()) {
            Node startNode = graphDb.findNode(MOVIE, "TMSId", movie1);
            Node endNode = graphDb.findNode(MOVIE, "TMSId", movie2);

            PathFinder<org.neo4j.graphdb.Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.allTypesAndDirections(), depth, limit);

            Iterable<org.neo4j.graphdb.Path> paths = finder.findAllPaths(startNode, endNode);

            for (org.neo4j.graphdb.Path p : paths) {
                weightedPaths.add(new WeightedPathImpl(p, "weight"));
            }
            Collections.sort(weightedPaths, comparator);
//            int ctt = 0;
//            for (WeightedPathImpl wp : weightedPaths) {
//                System.out.println(wp.getPath().toString());
//                System.out.println(wp.getWeight());
//                if (ctt > 5) {
//                    break;
//                }
//                ctt++;
//            }
            // result.addAll(Iterables.asList(finder.findAllPaths(startNode, endNode)));

            int cnt = 0;

            if (!sort) {
                for (org.neo4j.graphdb.Path p : paths) {

                    if (simple) {
                        resp.add(p.toString());
                    } else {
                        resp.add(render(p));
                    }
                    cnt++;
                    if (cnt >= limit) {
                        break;
                    }
                }
            }
            else {
                for(WeightedPathImpl wp: weightedPaths) {
                    if(simple) {
                        resp.add(wp.getPath().toString());
                    }
                    else {
                        resp.add(wp.render());
                    }
                    cnt++;
                    if (cnt >= limit) {
                        break;
                    }
                }
            }


            tx.success();
        } catch (NullPointerException | MultipleFoundException e) {
            resp.add(e.toString());
//            e.printStackTrace();
        }
        return Response.ok().entity(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resp)).build();
        // This works, yet super ugly
//        return Response.ok(gson.toJson(resp), MediaType.APPLICATION_JSON).build();

        // return Response.ok().entity(resp).type(MediaType.APPLICATION_JSON).build();
        // return Response.ok(gson.toJson(result)).build();
    }
    private Comparator<WeightedPathImpl> comparator = new Comparator<WeightedPathImpl>() {
        @Override
        public int compare(WeightedPathImpl o1, WeightedPathImpl o2) {
            if(o1.getWeight() > o2.getWeight()) {
                return -1;
            }
            else if (o1.getWeight() < o2.getWeight()) {
                return 1;
            }
            return 0;
        }
    };


    @GET
    @javax.ws.rs.Path("/single/movie/{movie1}/movie/{movie2}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findSinglePath(
            final @PathParam("movie1") String movie1,
            final @PathParam("movie2") String movie2,
            @QueryParam("depth") Integer depth) throws IOException {

        List<String> resp = new LinkedList<>();
        depth = depth == null ? 3 : depth;
        // Gson gson = new Gson();

        try (Transaction tx = graphDb.beginTx()) {
            Node startNode = graphDb.findNode(MOVIE, "TMSId", movie1);
            Node endNode = graphDb.findNode(MOVIE, "TMSId", movie2);

            PathFinder<org.neo4j.graphdb.Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.allTypesAndDirections(), depth);
            org.neo4j.graphdb.Path path = finder.findSinglePath(startNode, endNode);


            // result.addAll(Iterables.asList(finder.findAllPaths(startNode, endNode)));

            // resp.add(render(path, "TMSId"));
            if(path!= null) {
                //resp.add(path.toString());
                resp.add(render(path));
            }
            tx.success();
        } catch (NullPointerException | MultipleFoundException e) {
            resp.add(e.toString());
//            e.printStackTrace();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(resp)).build();

        // return Response.ok().entity(resp).type(MediaType.APPLICATION_JSON).build();
        // return Response.ok(gson.toJson(result)).build();
    }


    //
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