package ca.yorku.eecs;

import ca.yorku.eecs.handler.AddActorHandler;
import ca.yorku.eecs.handler.AddMovieGenreHandler;
import ca.yorku.eecs.handler.AddMovieHandler;
import ca.yorku.eecs.handler.AddRelationshipHandler;
import ca.yorku.eecs.handler.ComputeBaconNumberHandler;
import ca.yorku.eecs.handler.ComputeBaconPathHandler;
import ca.yorku.eecs.handler.GetActorHandler;
import ca.yorku.eecs.handler.GetMovieByGenreHandler;
import ca.yorku.eecs.handler.GetMovieHandler;
import ca.yorku.eecs.handler.HasRelationshipHandler;
import ca.yorku.eecs.handler.RestHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import java.util.concurrent.Executors;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class App {
    static int PORT = 8080;

    public static HttpServer server;
    public static DBUtil dbUtil;

    public static void start() throws IOException {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "123456"));
        dbUtil = DBUtil.getInstance(driver);
        RestHandler.setDbUtil(dbUtil);

        server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/api/v1/addActor", new AddActorHandler());
        server.createContext("/api/v1/getActor", new GetActorHandler());
        server.createContext("/api/v1/addMovie", new AddMovieHandler());
        server.createContext("/api/v1/getMovie", new GetMovieHandler());
        server.createContext("/api/v1/addRelationship", new AddRelationshipHandler());
        server.createContext("/api/v1/hasRelationship", new HasRelationshipHandler());
        server.createContext("/api/v1/computeBaconNumber", new ComputeBaconNumberHandler());
        server.createContext("/api/v1/computeBaconPath", new ComputeBaconPathHandler());
        server.createContext("/api/v1/addGenre", new AddMovieGenreHandler());
        server.createContext("/api/v1/getMoviesByGenre", new GetMovieByGenreHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.printf("Server started on port %d...\n", PORT);
    }

    public static void close() {
        server.stop(0);
    }

    public static void main(String[] args) throws IOException {
        start();
    }
}
