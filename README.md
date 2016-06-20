## Path Finder Based-on Neo4j

This is an example of an unmanaged-extension with Neo4j. 

### Data Model

The data model involved includes the following roles:

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
$ MATCH (a1:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(a2:Actor) where a1.personId >= "95000" CREATE (a1)-[:KNOWS]->(a2);

$ MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)
CREATE (m:Movie)-[:HAS]->(a:Actor);
```

The raw data is borrowed from gracenote. 

### Supported Path

a. movie-to-movie (single)
http://localhost:7474/service/path/single/movie/MV000000010000/movie/MV000000020000

b. movie-to-movie 
(all)
http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000000020000

(limit) 
http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000000020000?limit=5

(sort) 
http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000000020000?limit=50&sort=true

(simple)

http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000000020000?limit=50&sort=true&simple=true

(depth)

http://localhost:7474/service/path/all/movie/MV000000010000/movie/MV000331280000?limit=50&sort=true&simple=true&depth=2
(depth. aka: numbers of relationship)

c. algorithm:
(A*) 
localhost:7474/service/algorithm/astar/movie/MV000000010000/movie/MV000000020000

(dijkstra) 
http://localhost:7474/service/algorithm/dijk/movie/MV000000010000/movie/MV000000030000

d. actor-to-movie (single)
http://localhost:7474/service/neighbor/single/actor/Mariann%20%20Aalda/movie/MV000331280000

e. actor-to-movie(all)
http://localhost:7474/service/neighbor/all/actor/Mariann%20%20Aalda/movie/MV000233260000

f. actor-to-actor (single)
http://localhost:7474/service/neighbor/single/actor/Mariann%20%20Aalda/actor/Willie%20%20Aames

g. actor-to-actor(all)
http://localhost:7474/service/neighbor/all/actor/Mariann%20%20Aalda/actor/Willie%20%20Aames

h. movie-to-actor (single)
http://localhost:7474/service/neighbor/single/movie/MV000233260000/actor/Mariann%20%20Aalda/

i. movie-to-actor (all)
http://localhost:7474/service/neighbor/all/movie/MV000233260000/actor/Mariann%20%20Aalda/

### How to Run

# build the package
$ mvn clean package

# Copy the jar file to the /plugins path of Neo4j
$ cp target/neoRestful-1.0-SNAPSHOT.jar /usr/local/Cellar/neo4j/3.0.0/libexec/plugins

# append the configuration of extension 
$ vi conf/neo4j.conf
dbms.unmanaged_extension_classes=com.hulu.neo4j=/service

### Run Neo4j with Docker

```
docker run --publish=7474:7474 --publish=7687:7687 --volume=/Users/simei.he/Documents/graphDB/gracenote:/data --volume=/Users/simei.he/Documents/graphDB/gracenote/conf:/conf --volume=/usr/local/Cellar/neo4j/3.0.0/libexec/plugins:/plugins neo4j:3.0
```

### Cypher Tips

1 return relationship
```
$ match (n)-[r:KNOWS]->(m) return count(r);
9884034
```
2 return max value query
```
$ match (n:Actor) return max(n.personId);
99994
```

3 create extra relationship based on existing ones
```
$ MATCH (a1:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(a2:Actor) where a1.personId >= "95000" CREATE (a1)-[:KNOWS]->(a2);

$ MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)
CREATE (m:Movie)-[:HAS]->(a:Actor);
```

4 connect two movies

```
# One single path
# match (from:Movie {TMSId:"MV005347810000"}), (to:Movie {TMSId:"MV001759710000"}), path = shortestPath((from)-[*..3]-(to))
return path;

# All short paths
$ match (from:Movie {TMSId:"MV005347810000"}), (to:Movie {TMSId:"MV001759710000"}), path = allShortestPaths((from)-[*..3]-(to))
return path; 
```

5 bound the max depth of the relationships
nodes(path)
rels(path) (same as relationships(path))
# ??? to be verified
length(path).

6 fake relationship properties via cypher
```
# done
$ match (a1: Actor)-[r:KNOWS]->(a2: Actor) where a1.personId <= "3000" and a2.personId <= "3000" set r.weight=0.3;
# done
$ match (a1: Actor)-[r:KNOWS]->(a2: Actor) where a1.personId > "7000" and a2.personId > "7000" set r.weight=0.4;
# done

$ match (a1: Actor)-[r:KNOWS]->(a2: Actor) where a1.personId > "3000" and a2.personId <= "2000" set r.weight=0.2;
# to be 
$ match (a1: Actor)-[r:KNOWS]->(a2: Actor) where a1.personId > "4500" and a2.personId <= "7000" set r.weight=0.5;

# not done
$ match (a1: Actor)-[r:KNOWS]->(a2: Actor) where a1.personId <= "3000" and a2.personId > "3000" set r.weight=0.35;
$ match (a1: Actor)-[r:KNOWS]->(a2: Actor) where a1.personId > "3000" and a2.personId <= "3000" set r.weight=0.35;

# HAS: 0.6
$ match ()-[h:HAS]->() set h.weight=0.6;
# ACTED_IN: 0.6
$ match ()-[a:ACTED_IN]-() set a.weight=0.6;
```


### References
1. [Server Unmanaged Extensions](http://neo4j.com/docs/java-reference/current/#server-unmanaged-extensions)
2. [cyper tips](http://neo4j.com/docs/cypher-refcard/current/)
3. [develop manual](http://neo4j.com/docs/developer-manual/current/#getting-started)
