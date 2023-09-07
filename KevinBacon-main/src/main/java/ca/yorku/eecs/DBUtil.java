package ca.yorku.eecs;

import static org.neo4j.driver.v1.Values.parameters;

import ca.yorku.eecs.bean.Actor;
import ca.yorku.eecs.bean.Movie;
import ca.yorku.eecs.bean.Relationship;
import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.NotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.summary.SummaryCounters;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;

public class DBUtil {
    public static final String BACON_ID = "nm0000102";
    private Driver driver;
    private Session session;

    private static DBUtil dbUtil;

    private DBUtil(Driver driver) {
        this.driver = driver;

        session = driver.session();
    }

    public static DBUtil getInstance(Driver driver) {
        if (dbUtil == null) {
            dbUtil = new DBUtil(driver);
        }

        return dbUtil;
    }

    public boolean addActor(String name, String actorId) throws BadRequestException {
        List<Actor> actors = getActor(actorId);
        if (actors != null && !actors.isEmpty()) {
            throw new BadRequestException("Actor " + actorId + " already exists");
        }

        SummaryCounters counters = session.writeTransaction(new TransactionWork<SummaryCounters>() {
            @Override
            public SummaryCounters execute(Transaction tx) {
                StatementResult result = tx.run("CREATE (a: Actor {name: $name, id: $actorId});", parameters("name", name, "actorId", actorId));
                return result.consume().counters();
            }
        });

        return counters.nodesCreated() == 1;
    }

    public void clear() {
        session.writeTransaction(new TransactionWork<SummaryCounters>() {

            @Override
            public SummaryCounters execute(Transaction tx) {
                return tx.run("MATCH (a )-[r:ACTED_IN]->() delete r").summary().counters();
            };
        });
        session.writeTransaction(new TransactionWork<SummaryCounters>() {

            @Override
            public SummaryCounters execute(Transaction tx) {
                return tx.run("MATCH (m: Movie) DELETE m").summary().counters();
            };
        });
        session.writeTransaction(new TransactionWork<SummaryCounters>() {

            @Override
            public SummaryCounters execute(Transaction tx) {
                return tx.run("MATCH (a: Actor) DELETE a").summary().counters();
            };
        });
    }

    public List<Actor> getActor(String actorId) {
        return session.readTransaction(new TransactionWork<List<Actor>>() {

            @Override
            public List<Actor> execute(Transaction tx) {
                StatementResult result = tx.run("MATCH (a: Actor) WHERE a.id = $actorId RETURN a.name as name, a.id as actorId;", parameters("actorId", actorId));
                return result.list(record -> new Actor(record.get("name").asString(), record.get("actorId").asString()));
            }
        });
    }

    public boolean addMovie(String name, String movieId) throws BadRequestException {
        List<Movie> movies = getMovie(movieId);
        if (movies != null && !movies.isEmpty()) {
            throw new BadRequestException("Movie " + movieId + " already exists");
        }

        SummaryCounters counters = session.writeTransaction(new TransactionWork<SummaryCounters>() {
            @Override
            public SummaryCounters execute(Transaction tx) {
                StatementResult result = tx.run("CREATE (m: Movie {name: $name, id: $movieId});", parameters("name", name, "movieId", movieId));
                return result.consume().counters();
            }
        });

        return counters.nodesCreated() == 1;
    }

    public boolean addMovieGenre(String movieId, String genre) throws NotFoundException, BadRequestException {
        Movie movie = checkMovie(movieId);

        if (movie.getGenre() != null && movie.getGenre().equals(genre)) {
            throw new BadRequestException("Movie with id " + movieId + " with genre " + genre + " already exists");
        }

        SummaryCounters counters = session.writeTransaction(new TransactionWork<SummaryCounters>() {
            @Override
            public SummaryCounters execute(Transaction tx) {
                String query = "MATCH (m: Movie { id: $movieId})\n"
                        + "SET m.genre = $genre RETURN m;";
                StatementResult result = tx.run(query, parameters( "movieId", movieId, "genre", genre));
                return result.consume().counters();
            }
        });

        return counters.propertiesSet() == 1;
    }

    public List<Movie> getMovieByGenre(String genre) {
        return session.readTransaction(new TransactionWork<List<Movie>>() {

            @Override
            public List<Movie> execute(Transaction tx) {
                StatementResult result = tx.run("MATCH (m: Movie) WHERE m.genre = $genre RETURN m.name as name, m.id as movieId;", parameters("genre", genre));
                return result.list(record -> createMovie(record));
            }
        });
    }

    public List<Movie> getMovie(String movieId) {
        return session.readTransaction(new TransactionWork<List<Movie>>() {

            @Override
            public List<Movie> execute(Transaction tx) {
                StatementResult result = tx.run("MATCH (m: Movie) WHERE m.id = $movieId RETURN m.name as name, m.id as movieId, m.genre as genre;", parameters("movieId", movieId));
                return result.list(record -> createMovie(record));
            }
        });
    }

    public Movie createMovie(Record record) {
        Movie movie = new Movie(record.get("name").asString(), record.get("movieId").asString());
        if (record.containsKey("genre")) {
            movie.setGenre(record.get("genre").asString());
        }

        return movie;
    }

    public boolean addRelationship(String actorId, String movieId) throws BadRequestException, NotFoundException {
        List<Relationship> relationships = getRelations(actorId, movieId);
        if (relationships != null && !relationships.isEmpty()) {
            throw new BadRequestException("Relationship between actor " + actorId + " and movie " + movieId + " already exists");
        }

        SummaryCounters counters = session.writeTransaction(new TransactionWork<SummaryCounters>() {
            @Override
            public SummaryCounters execute(Transaction tx) {
                String query = "MATCH (a: Actor), (m: Movie)\n"
                        + "WHERE a.id = $actorId AND m.id = $movieId CREATE (a)-[r:ACTED_IN]->(m)\n"
                        + "RETURN type(r);";

                StatementResult result = tx.run(query, parameters("actorId", actorId, "movieId", movieId));
                return result.consume().counters();
            }
        });

        return counters.relationshipsCreated() == 1;
    }

    public List<Relationship> getRelations(String actorId, String movieId) throws NotFoundException {
        checkActor(actorId);
        checkMovie(movieId);

        return session.readTransaction(new TransactionWork<List<Relationship>>() {

            @Override
            public List<Relationship> execute(Transaction tx) {
                String match = "MATCH (a: Actor)-[r: ACTED_IN]->(m: Movie) ";
                String where = "where a.id=$actorId and m.id=$movieId";
                String fields = " return a.id as actorId, m.id as movieId";
                if (actorId == null) {
                    where = "where m.id=$movieId";
                } else if (movieId == null) {
                    where = "where a.id=$actorId";
                }

                String query = match + where + fields;

                StatementResult result = tx.run(query,
                        parameters("actorId", actorId, "movieId", movieId));
                return result.list(record -> new Relationship(
                        record.get("actorId").asString(),
                        record.get("movieId").asString()));
            }
        });
    }

    public int computeBaconNumber(String actorId) throws BadRequestException, NotFoundException {
        checkBacon();
        if (actorId.equals(BACON_ID)) {
            return 0;
        }
        checkActor(actorId);

        List<Actor> target = getActor(actorId);
        if (target == null || target.isEmpty()) {
            throw new BadRequestException("Actor not found");
        }

        int number = session.readTransaction(new TransactionWork<Integer>() {

            @Override
            public Integer execute(Transaction tx) {
                String query = "MATCH p=shortestPath((a:Actor{id: $actorId})-[*]-(b:Actor{id: $baconId})) RETURN length(p)/2 as baconNumber";
                StatementResult result = tx.run(query, parameters("actorId", actorId, "baconId", BACON_ID));
                if (result.hasNext()) {
                    return result.peek().get("baconNumber").asInt();
                }

                return -1;
            }
        });

        if (number == -1) {
            throw new NotFoundException();
        }

        return number;
    }

    private void checkBacon() throws NotFoundException {
        checkActor(BACON_ID);
    }

    private void checkActor(String actorId) throws NotFoundException {
        if (actorId == null) {
            return;
        }

        List<Actor> target = getActor(actorId);
        if (target == null || target.isEmpty()) {
            throw new NotFoundException();
        }
    }

    private Movie checkMovie(String movieId) throws NotFoundException {
        if (movieId == null) {
            return null;
        }

        List<Movie> target = getMovie(movieId);
        if (target == null || target.isEmpty()) {
            throw new NotFoundException();
        }

        return target.get(0);
    }

    public List<String> computeBaconPath(String actorId) throws NotFoundException, JSONException {
        checkBacon();
        if (actorId.equals(BACON_ID)) {
            return new ArrayList<>();
        }
        checkActor(actorId);

        return session.readTransaction(new TransactionWork<List<String>>() {

            @Override
            public List<String> execute(Transaction tx) {
                String query = "MATCH p=shortestPath((a:Actor{id: $actorId})-[*]-(b:Actor{id: $baconId})) RETURN p as baconPath";
                StatementResult result = tx.run(query, parameters("actorId", actorId, "baconId", BACON_ID));
                if (result.hasNext()) {
                    List<String> pathIds = new ArrayList<>();
                    Path path = result.peek().get("baconPath").asPath();
                    Iterator<Node> nodeIterator = path.nodes().iterator();
                    while (nodeIterator.hasNext()) {
                        Node node = nodeIterator.next();
                        pathIds.add(node.get("id").asString());
                    }

                    return pathIds;
                }

                return null;
            }
        });
    }

    public static void main(String[] args) throws BadRequestException, NotFoundException, JSONException {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "123456"));
        DBUtil dbUtil = getInstance(driver);

        System.out.println(dbUtil.addMovie("Wild Things (1998)", "nm9894331"));
        System.out.println(dbUtil.addActor("Robert Wagner (I)", "nm0000103"));
        System.out.println(dbUtil.addMovie("All the Fine Young Cannibals (1960)", "nm9894332"));
        System.out.println(dbUtil.addActor("Louise Beavers", "nm0000104"));
        System.out.println(dbUtil.addMovie("Coquette (1929)", "nm9894333"));
        System.out.println(dbUtil.addActor("Mary Pickford", "nm0000105"));

        System.out.println(dbUtil.addActor("Kevin Bacon", BACON_ID));
        System.out.println(dbUtil.addRelationship(BACON_ID, "nm9894331"));
        System.out.println(dbUtil.addRelationship("nm0000103", "nm9894331"));
        System.out.println(dbUtil.addRelationship("nm0000103", "nm9894332"));
        System.out.println(dbUtil.addRelationship("nm0000104", "nm9894332"));
        System.out.println(dbUtil.addRelationship("nm0000104", "nm9894333"));
        System.out.println(dbUtil.addRelationship("nm0000105", "nm9894333"));

        System.out.println(dbUtil.addActor("i1", "n1"));
        System.out.println(dbUtil.addActor("i2", "n2"));
        System.out.println(dbUtil.addActor("i4", "n4"));
        System.out.println(dbUtil.addMovie("m1", "m1"));
        System.out.println(dbUtil.addMovieGenre("m1", "asdasa"));
        System.out.println(dbUtil.computeBaconPath("nm0000105"));
    }
}
