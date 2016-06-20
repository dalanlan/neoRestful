package com.hulu.neo4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphalgo.*;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by simei.he on 6/16/16.
 */

@javax.ws.rs.Path("/algorithm")
public class AlgorithmResource {
    private final GraphDatabaseService graphDb;
    private final ObjectMapper objectMapper;

    private static final Label MOVIE = Label.label("Movie");

    public AlgorithmResource(@Context GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        this.objectMapper = new ObjectMapper();
    }

    @GET
    @javax.ws.rs.Path("/astar/movie/{movie1}/movie/{movie2}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findAstarPath(
            final @PathParam("movie1") String movie1,
            final @PathParam("movie2") String movie2) throws IOException {

        List<String> result = new LinkedList<>();

        try (Transaction tx = graphDb.beginTx()) {
            Node startNode = graphDb.findNode(MOVIE, "TMSId", movie1);
            Node endNode = graphDb.findNode(MOVIE, "TMSId", movie2);

            PathFinder<WeightedPath> astar = GraphAlgoFactory.aStar(PathExpanders.allTypesAndDirections(),
                    costEvaluator,
                    estimateEvaluator
            );
            WeightedPath path = astar.findSinglePath(startNode, endNode);
            System.out.println(path.weight());

            if (path != null) {
                result.add(PathResource.render(path));
            }
            tx.success();
        } catch (NullPointerException | MultipleFoundException e) {
//            e.printStackTrace();
            result.add(e.toString());
        }

        return Response.ok().entity(objectMapper.writeValueAsString(result)).build();

    }

    @GET
    @javax.ws.rs.Path("/dijk/movie/{movie1}/movie/{movie2}")
    public Response findDijkstraPath(
            final @PathParam("movie1") String movie1,
            final @PathParam("movie2") String movie2) throws IOException {

        List<String> result = new LinkedList<>();
        try (Transaction tx = graphDb.beginTx()) {
            Node startNode = graphDb.findNode(MOVIE, "TMSId", movie1);
            Node endNode = graphDb.findNode(MOVIE, "TMSId", movie2);

            PathFinder<WeightedPath> dijk = GraphAlgoFactory.dijkstra(PathExpanders.allTypesAndDirections(),
                    costEvaluator);
            WeightedPath path = dijk.findSinglePath(startNode, endNode);
            System.out.println(path.weight());
            if (path != null) {
                result.add(PathResource.render(path));
            }
            tx.success();
        } catch (NullPointerException | MultipleFoundException e) {
//            e.printStackTrace();
            result.add(e.toString());
        }
        return Response.ok().entity(objectMapper.writeValueAsString(result)).build();

    }

    private EstimateEvaluator<Double> estimateEvaluator = new EstimateEvaluator<Double>() {
        @Override
        public Double getCost(Node node, Node node1) {
//            double start = 0.0;
//            double end = 1.0;
//            double random = new Random().nextDouble();
//            double res = start + (random * (end - start));
//            return res;
            return 1.0;
        }
    };
    private CostEvaluator<Double> costEvaluator = new CostEvaluator<Double>() {
        @Override
        public Double getCost(Relationship relationship, Direction direction) {
            Object valueOrNull = relationship.getProperty("weight", null);
            if(valueOrNull != null) {
                return 1.0-(Double)valueOrNull;
            }
            return 1.0;
        }


    };
}
