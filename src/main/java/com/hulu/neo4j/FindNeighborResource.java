package com.hulu.neo4j;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.register.Register;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by simei.he on 6/12/16.
 */

@Path("/neighbor")

public class FindNeighborResource {

    private final GraphDatabaseService graphDb;
    private final ObjectMapper objectMapper;


    public FindNeighborResource (@Context GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        this.objectMapper = new ObjectMapper();
    }

    private StreamingOutput newStream (String cypherQuery, Map params) {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8);
                jg.writeStartObject();
                jg.writeFieldName("neighbor");
                jg.writeStartArray();
                try (Transaction tx = graphDb.beginTx();
                     Result result = graphDb.execute(cypherQuery, params)) {
                    while (result.hasNext()) {
                        Map<String, Object> row = result.next();
                        jg.writeString((row.get("path")).toString());
                    }
                    tx.success();
                }
                jg.writeEndArray();
                jg.writeEndObject();
                jg.flush();
                jg.close();
            }
        };
        return stream;
    }

    @GET
    @Path("/single/movie/{movie1}/movie/{movie2}")
    public Response findSingleMMNeighbor(final @PathParam("movie1") String movie1, final @PathParam("movie2") String movie2) {
        final Map params = new HashMap<>();
        params.put("movie1", movie1);
        params.put("movie2", movie2);
        StreamingOutput stream = newStream(singleMMNeighbor(), params);
        return Response.ok().entity( stream ).type(MediaType.APPLICATION_JSON).build();
    }

    private String singleMMNeighbor() {
        return "match (from:Movie {TMSId: {movie1} }), (to:Movie {TMSId:{movie2}}), path = shortestPath((from)-[*..3]-(to)) return path";
    }

    @GET
    @Path("/all/movie/{movie1}/movie/{movie2}")
    public Response findAllMMNeighbor(final @PathParam("movie1") String movie1, final @PathParam("movie2") String movie2) {
        final Map params = new HashMap<>();
        params.put("movie1", movie1);
        params.put("movie2", movie2);
        StreamingOutput stream = newStream(allMMNeighbor(), params);
        return Response.ok().entity( stream ).type(MediaType.APPLICATION_JSON).build();
    }
    private String allMMNeighbor() {
        return "match (from:Movie {TMSId:{movie1}}), (to:Movie {TMSId:{movie2}}), path = allShortestPaths((from)-[*..3]-(to)) return path";
    }

    @GET
    @Path("/single/actor/{actor}/movie/{movie}")
    public Response findSingleAMNeighbor(final @PathParam("movie") String movie, final @PathParam("actor") String actor) {
        final Map params = new HashMap<>();
        params.put("movie", movie);
        params.put("actor", actor);
        StreamingOutput stream = newStream(singleAMNeighbor(), params);
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
    }
    private String singleAMNeighbor() {
        return "match (from: Actor {name: {actor}}), (to: Movie {TMSId: {movie}}), path = shortestPath((from)-[*..3]-(to)) return path";
    }

    @GET
    @Path("/all/actor/{actor}/movie/{movie}")
    public Response findAllAMNeighbor(final @PathParam("movie") String movie, final @PathParam("actor") String actor) {
        final Map params = new HashMap<>();
        params.put("movie", movie);
        params.put("actor", actor);
        StreamingOutput stream = newStream(allAMNeighbor(), params);
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
    }
    private String allAMNeighbor() {
        return "match (from: Actor {name: {actor}}), (to: Movie {TMSId: {movie}}), path = allShortestPaths((from)-[*..3]-(to)) return path";
    }

    @GET
    @Path("/single/movie/{movie}/actor/{actor}")
    public Response findSingleMANeighbor(final @PathParam("movie") String movie, final @PathParam("actor") String actor) {
        final Map params = new HashMap<>();
        params.put("movie", movie);
        params.put("actor", actor);
        StreamingOutput stream = newStream(singleMANeighbor(), params);
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
    }
    private String singleMANeighbor() {
        return "match (from: Movie {TMSId: {movie}}), (to: Actor {name: {actor}}), path = shortestPath((from)-[*..3]-(to)) return path";
    }

    @GET
    @Path("/all/movie/{movie}/actor/{actor}")
    public Response findAllMANeighbor(final @PathParam("movie") String movie, final @PathParam("actor") String actor) {
        final Map params = new HashMap<>();
        params.put("movie", movie);
        params.put("actor", actor);
        StreamingOutput stream = newStream(allMANeighbor(), params);
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
    }
    private String allMANeighbor() {
        return "match (from: Movie {TMSId: {movie}}), (to: Actor {name: {actor}}), path = allShortestPaths((from)-[*..3]-(to)) return path";
    }

    @GET
    @Path("/single/actor/{actor1}/actor/{actor2}")
    public Response findSingleAANeighbor(final @PathParam("actor1") String actor1, final @PathParam("actor2") String actor2) {
        final Map params = new HashMap<>();
        params.put("actor1", actor1);
        params.put("actor2", actor2);
        StreamingOutput stream = newStream(singleAANeighbor(), params);
        return Response.ok().entity( stream ).type(MediaType.APPLICATION_JSON).build();
    }

    private String singleAANeighbor() {
        return "match (from:Actor {name: {actor1} }), (to:Actor {name:{actor2}}), path = shortestPath((from)-[*..3]-(to)) return path";
    }

    @GET
    @Path("/all/actor/{actor1}/actor/{actor2}")
    public Response findAllAANeighbor(final @PathParam("actor1") String actor1, final @PathParam("actor2") String actor2) {
        final Map params = new HashMap<>();
        params.put("actor1", actor1);
        params.put("actor2", actor2);
        StreamingOutput stream = newStream(allAANeighbor(), params);
        return Response.ok().entity( stream ).type(MediaType.APPLICATION_JSON).build();
    }
    private String allAANeighbor() {
        return "match (from:Actor {name: {actor1} }), (to:Actor {name:{actor2}}), path = allShortestPaths((from)-[*..3]-(to)) return path";
    }
}
