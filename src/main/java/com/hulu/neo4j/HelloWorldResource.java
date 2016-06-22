package com.hulu.neo4j;

/**
 * Created by simei.he on 6/6/16.
 */



import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.string.UTF8;


@Path( "/helloworld" )
public class HelloWorldResource
{
    private final GraphDatabaseService database;
    private static final Label ACTOR = Label.label("Actor");
    private static final Label MOVIE = Label.label("Movie");
    public HelloWorldResource( @Context GraphDatabaseService database )
    {
        this.database = database;
    }

    @GET
    @Produces( MediaType.TEXT_PLAIN )
    @Path( "/{nodeId}" )
    public Response hello( @PathParam( "nodeId" ) long nodeId )
    {
        // Do stuff with the database
        String call;
        try (Transaction tx = database.beginTx()) {
            Node node = database.getNodeById(nodeId);

            if(node.hasLabel(ACTOR)) {
                call = "Actor: " + node.getProperties("name").toString();
            }
            else if(node.hasLabel(MOVIE)) {
                call = "Movie: " + node.getProperties("TMSId").toString();
            }
            else {
                return Response.status( Status.NOT_FOUND ).entity("Request Id not found").type(MediaType.APPLICATION_JSON).build();
            }
            tx.success();
        }

        return Response.status( Status.OK ).entity( UTF8.encode( "Hello World, " + call )).build();
    }
}
