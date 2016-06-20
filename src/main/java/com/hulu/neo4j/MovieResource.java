package com.hulu.neo4j;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.codegen.Parameter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.register.Register;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import static org.neo4j.server.rest.domain.TraverserReturnType.path;

/**
 * Created by simei.he on 6/12/16.
 */

@Path("/movie")
public class MovieResource {
    private final GraphDatabaseService graphDb;
    private final ObjectMapper objectMapper;

    public MovieResource(@Context GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        this.objectMapper = new ObjectMapper();
    }

    @GET
    @Path("{movieTMSId}")
    public Response showMovie(final @PathParam("movieTMSId") String TMSId) {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8);
                jg.writeStartObject();
                jg.writeFieldName("movie");
                jg.writeStartArray();
                try (Transaction tx = graphDb.beginTx();
                     Result result = graphDb.execute("MATCH (m:Movie) WHERE m.TMSId={tms} RETURN m",
                             Collections.singletonMap("tms", TMSId))) {
                    while (result.hasNext()) {
                        Map<String, Object> row = result.next();
                        jg.writeString((row.get("m")).toString());

//                        for (String key : result.columns()) {
//                            System.out.printf("%s=%s%n", key, row.get(key));
//                        }
                    }
                    tx.success();
                }
                jg.writeEndArray();
                jg.writeEndObject();
                jg.flush();
                jg.close();
            }


        };
        return Response.ok().entity( stream ).type(MediaType.APPLICATION_JSON).build();

    }
}
