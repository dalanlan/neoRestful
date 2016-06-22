package com.hulu.neo4j;

import com.hulu.neo4j.util.RenderUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 * Created by simei.he on 6/14/16.
 */

@javax.ws.rs.Path("/path")
public class PathResource {
    private final GraphDatabaseService graphDb;
    private final ObjectMapper objectMapper;
    private static final Label MOVIE = Label.label("Movie");


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

            int cnt = 0;

            if (!sort) {
                for (org.neo4j.graphdb.Path p : paths) {

                    if (simple) {
                        resp.add(p.toString());
                    } else {
                        resp.add(RenderUtil.render(p));
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
            @DefaultValue("3")@QueryParam("depth") Integer depth) throws IOException {

        List<String> resp = new LinkedList<>();

        try (Transaction tx = graphDb.beginTx()) {
            Node startNode = graphDb.findNode(MOVIE, "TMSId", movie1);
            Node endNode = graphDb.findNode(MOVIE, "TMSId", movie2);

            PathFinder<org.neo4j.graphdb.Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.allTypesAndDirections(), depth);
            org.neo4j.graphdb.Path path = finder.findSinglePath(startNode, endNode);


            if(path!= null) {
                resp.add(RenderUtil.render(path));
            }
            tx.success();
        } catch (NullPointerException | MultipleFoundException e) {
            resp.add(e.toString());
//            e.printStackTrace();
        }
        return Response.ok().entity(objectMapper.writeValueAsString(resp)).build();

    }



}