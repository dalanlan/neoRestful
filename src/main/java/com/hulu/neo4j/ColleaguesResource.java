package com.hulu.neo4j;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

import java.util.*;
import java.util.stream.Stream;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * Created by simei.he on 6/7/16.
 */

@Path("/colleagues")
public class ColleaguesResource {
    private final GraphDatabaseService graphDb;
    private final ObjectMapper objectMapper;

    private static final RelationshipType ACTED_IN = RelationshipType.withName("ACTED_IN");
    private static final Label ACTOR = Label.label("Actor");

    public ColleaguesResource(@Context GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        this.objectMapper = new ObjectMapper();
    }

    @GET
    @Path("/{actorName}")
    public Response findColleagues(final @PathParam("actorName") String actorName, final @QueryParam("limit") Integer limit) {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8);
                jg.writeStartObject();


                Map<Node, Integer> nodeMap = new HashMap<>();

                try (Transaction tx = graphDb.beginTx();
                     ResourceIterator<Node> actors = graphDb.findNodes(ACTOR, "name", actorName)) {


                    while (actors.hasNext()) {
                        Node actor = actors.next();
                        for (Relationship actedIn : actor.getRelationships(ACTED_IN, OUTGOING)) {
                            Node endNode = actedIn.getEndNode();
                            for (Relationship colleagueActedIn : endNode.getRelationships(ACTED_IN, INCOMING)) {
                                Node colleague = colleagueActedIn.getStartNode();

                                if (!colleague.equals(actor)) {
                                    int cnt = nodeMap.containsKey(colleague) ? nodeMap.get(colleague) : 0;
                                    nodeMap.put(colleague, cnt+1);

                                }
                            }
                        }
                    }


                    ValueComparator bvc = new ValueComparator(nodeMap);
                    TreeMap<Node, Integer> sorted_map = new TreeMap<>(bvc);
                    sorted_map.putAll(nodeMap);

                    int realLimit = limit == null ? Integer.MAX_VALUE : limit;
                    int cnt = 0;
                    jg.writeFieldName("colleagues");
                    jg.writeStartArray();
                    for(Map.Entry<Node, Integer> entry : sorted_map.entrySet()) {
                        jg.writeString(entry.getKey().getProperties("name").toString());
                        cnt++;
                        if(cnt == realLimit) {
                            break;
                        }
                    }
                    jg.writeEndArray();

                    tx.success();
                }

                jg.writeEndObject();
                jg.flush();
                jg.close();


            }
        };
        return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON_TYPE).build();
    }


}


class ValueComparator implements Comparator<Node> {
    Map<Node, Integer> base;

    public ValueComparator(Map<Node, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(Node a, Node b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}