## Path Finder Based-on Neo4j

This is an example of an unmanaged-extension with Neo4j. 

### Data Model

The raw data is borrowed from gracenote. And the data model involved includes the following roles:

|Node:Label|ID|Property|
|:---:|:---:|:--:|:----:|
|movie|[TMSId]||
|actor|[personId]|name|

|Relationship:Type|Property|StartNode|EndNode|
|:---:|:---:|:---:|:---:|
|ACTED_IN|weight|Actor|Movie|
|KNOWS|weight|Actor|Actor|
|HAS|weight|Movie|Actor|

The `ACTED_IN` relationship is natural, while the others `KNOWS` and `HAS` are built based on `ACTED_IN`.

```
# wrote in cypher
$ MATCH (a1:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(a2:Actor) where a1.personId >= "95000" CREATE (a1)-[:KNOWS]->(a2);

$ MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)
CREATE (m:Movie)-[:HAS]->(a:Actor);
```


### Supported Query

You can tell from the name that this project mainly focuses on the path query between
two entities.

I didn't introduce swagger, and hopefully you won't lose yourself here.


|basePath|paths|path params|query params|description|
|:--:|:--:|:--:|:--:|:--:|
|/service|/path/single/movie/{movie1}/movie/{movie2}|`{movie1}` is the TMSId of a movie, while `{movie2}` is of another||return a single shortest path of two movies based on the query params|
|/service|/path/all/movie/{movie1}}/movie/{movie2}|`{movie1}` is the TMSId of a movie, while `{movie2}` is of another|`limit`: defines the number of paths returned; `sort`: indicates whether we should return a sorted result or not, not sorted by default; `simple`: prints the metadata of the node of the path or not, printed by default; `depth`: defines the number of relationship involved, 3 by default.|return all of shortest paths of two movies based on the query params|
|/service|/algorithm/astar/movie/{movie1}/movie/{movie2}|`{movie1}` is the TMSId of a movie, while `{movie2}` is of another||return the cheapest path calculated by Astar between two movies|
|/service|/algorithm/dijk/movie/{movie1}/movie/{movie2}|`{movie1}` is the TMSId of a movie, while `{movie2}` is of another||return the cheapest path calculated by dijkstra between two movies|
|/service|/neighbor/single/actor/{actor}/movie/{movie}|`{actor}` is the name of an actor, while `{movie}` is the TMSId of a movie||return the single shortest path of an actor and a movie|
|/service|/neighbor/all/actor/{actor}/movie/{movie}|`{actor}` is the name of an actor, while `{movie}` is the TMSId of a movie||return all of the shortest paths of an actor and a movie|
|/service|/neighbor/single/actor/{actor1}/actor/{actor2}|`{actor1}` is the name of an actor, while `{actor2}` is of another||return a single shortest path between two actors|
|/service|/neighbor/single/movie/{movie}/actor/{actor}|`{actor}` is the name of an actor, while `{movie}` is the TMSId of a movie||return the single shortest path of an actor and a movie|
|/service|/neighbor/all/movie/{movie}/actor/{actor}|`{actor}` is the name of an actor, while `{movie}` is the TMSId of a movie||return all of shortest paths of an actor and a movie|


For example, you can access the following endpoints:

```
http://localhost:7474/service/path/single/movie/MV000000010000/movie/MV000000020000

http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000000020000

http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000000020000?limit=5

http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000000020000?limit=50&sort=true

http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000000020000?limit=50&sort=true&simple=true

http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000331280000?limit=50&sort=true&simple=true&depth=2

http://localhost:7474/service/algorithm/astar/movie/MV000000010000/movie/MV000000020000

http://localhost:7474/service/algorithm/dijk/movie/MV000000010000/movie/MV000000030000

http://localhost:7474/service/neighbor/single/actor/Mariann%20%20Aalda/movie/MV000331280000

http://localhost:7474/service/neighbor/all/actor/Mariann%20%20Aalda/movie/MV000233260000

http://localhost:7474/service/neighbor/single/actor/Mariann%20%20Aalda/actor/Willie%20%20Aames

http://localhost:7474/service/neighbor/all/actor/Mariann%20%20Aalda/actor/Willie%20%20Aames

http://localhost:7474/service/neighbor/single/movie/MV000233260000/actor/Mariann%20%20Aalda/

http://localhost:7474/service/neighbor/all/movie/MV000233260000/actor/Mariann%20%20Aalda/
```

### How to Run 

```
# build the package
$ mvn clean package

# Copy the jar file to the /plugins path of Neo4j
$ cp target/neoRestful-1.0-SNAPSHOT.jar {neo4j-plugin-dir}

# append the configuration of extension 
$ vi conf/neo4j.conf
dbms.unmanaged_extension_classes=com.hulu.neo4j=/service
```

### Run Neo4j with Docker

```
docker run --publish=7474:7474 --publish=7687:7687 --volume=/Users/simei.he/Documents/graphDB/gracenote:/data --volume=/Users/simei.he/Documents/graphDB/gracenote/conf:/conf --volume=/usr/local/Cellar/neo4j/3.0.0/libexec/plugins:/plugins neo4j:3.0
```


### References
1. [Server Unmanaged Extensions](http://neo4j.com/docs/java-reference/current/#server-unmanaged-extensions)
2. [cyper tips](http://neo4j.com/docs/cypher-refcard/current/)
3. [develop manual](http://neo4j.com/docs/developer-manual/current/#getting-started)
