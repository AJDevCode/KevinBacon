package ca.yorku.eecs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.UUID;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.json.JSONObject;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue( true );
    }

    public void setUp() throws Exception {
        App.start();
        App.dbUtil.clear();
    }

    public void tearDown() throws Exception {
        App.close();
    }

    private HttpURLConnection getRequest(String endpoint) throws Exception {
        URL url = new URL("http://localhost:8080/api/v1/" + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setRequestMethod("GET");

        return conn;
    }

    private HttpURLConnection putRequest(String endpoint, JSONObject param) throws Exception {
        URL url = new URL("http://localhost:8080/api/v1/" + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json;");
        conn.setRequestMethod("PUT");

        conn.setDoOutput(true);
        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
        osw.write(param.toString());
        osw.flush();

        return conn;
    }

    public void testAddActorPass() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("actorId", UUID.randomUUID().toString());
        jsonObject.put("name", "newActor");
        HttpURLConnection conn = putRequest("addActor", jsonObject);

        assertEquals(200, conn.getResponseCode());
    }

    public void testAddActorFail() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("actorId", UUID.randomUUID().toString());
        HttpURLConnection conn = putRequest("addActor", jsonObject);

        assertEquals(400, conn.getResponseCode());
    }

    public void testAddMoviePass() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("movieId", UUID.randomUUID().toString());
        jsonObject.put("name", "newMovie");
        HttpURLConnection conn = putRequest("addMovie", jsonObject);

        assertEquals(200, conn.getResponseCode());
    }

    public void testAddMovieFail() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("movieId", UUID.randomUUID().toString());
        HttpURLConnection conn = putRequest("addMovie", jsonObject);

        assertEquals(400, conn.getResponseCode());
    }

    public void testAddRelationshipPass() throws Exception {
        String actorId = UUID.randomUUID().toString();
        String movieId = UUID.randomUUID().toString();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("actorId", actorId);
        jsonObject.put("name", "newActor");
        HttpURLConnection conn = putRequest("addActor", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        jsonObject.put("movieId", movieId);
        jsonObject.put("name", "newMovie");
        conn = putRequest("addMovie", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        jsonObject.put("movieId", movieId);
        jsonObject.put("actorId", actorId);

        conn = putRequest("addRelationship", jsonObject);
        assertEquals(200, conn.getResponseCode());
    }

    public void testAddRelationshipFail() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("movieId", UUID.randomUUID().toString());

        HttpURLConnection conn = putRequest("addRelationship", jsonObject);
        assertEquals(400, conn.getResponseCode());

        jsonObject.put("actorId", UUID.randomUUID().toString());

        conn = putRequest("addRelationship", jsonObject);
        assertEquals(404, conn.getResponseCode());
    }

    public void testGetActorPass() throws Exception {
        String actorId = UUID.randomUUID().toString();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("actorId", actorId);
        jsonObject.put("name", "newActor");
        HttpURLConnection conn = putRequest("addActor", jsonObject);
        assertEquals(200, conn.getResponseCode());

        conn.disconnect();

        conn = getRequest("getActor?actorId=" + actorId);
        assertEquals(200, conn.getResponseCode());
    }

    public void testGetActorFailed() throws Exception {
        String actorId = UUID.randomUUID().toString();

        HttpURLConnection conn = getRequest("getActor?actorId=" + actorId);
        assertEquals(404, conn.getResponseCode());

        conn = getRequest("getActor?a=1");
        assertEquals(400, conn.getResponseCode());
    }

    public void testGetMoviePass() throws Exception {
        String movieId = UUID.randomUUID().toString();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("movieId", movieId);
        jsonObject.put("name", "newActor");
        HttpURLConnection conn = putRequest("addMovie", jsonObject);
        assertEquals(200, conn.getResponseCode());

        conn.disconnect();

        conn = getRequest("getMovie?movieId=" + movieId);
        assertEquals(200, conn.getResponseCode());
    }

    public void testGetMovieFailed() throws Exception {
        String movieId = UUID.randomUUID().toString();

        HttpURLConnection conn = getRequest("getMovie?movieId=" + movieId);
        assertEquals(404, conn.getResponseCode());

        conn = getRequest("getMovie?a=1");
        assertEquals(400, conn.getResponseCode());
    }

    public void testHasRelationshipPass() throws Exception {
        String actorId = UUID.randomUUID().toString();
        String movieId = UUID.randomUUID().toString();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("actorId", actorId);
        jsonObject.put("name", "newActor");
        HttpURLConnection conn = putRequest("addActor", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        jsonObject.put("movieId", movieId);
        jsonObject.put("name", "newMovie");
        conn = putRequest("addMovie", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        jsonObject.put("movieId", movieId);
        jsonObject.put("actorId", actorId);

        conn = putRequest("addRelationship", jsonObject);
        assertEquals(200, conn.getResponseCode());

        conn = getRequest("hasRelationship?movieId=" + movieId + "&actorId=" + actorId);
        assertEquals(200, conn.getResponseCode());
    }

    public void testHasRelationshipFail() throws Exception {
        HttpURLConnection conn = getRequest("hasRelationship?movieId=" + UUID.randomUUID().toString() + "&actorId=1");
        assertEquals(404, conn.getResponseCode());

        conn = getRequest("hasRelationship?movieI=" + UUID.randomUUID().toString() + "&actorId=1");
        assertEquals(400, conn.getResponseCode());
    }

    public void testComputeBaconNumberPass() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("actorId", DBUtil.BACON_ID);
        jsonObject.put("name", "Kevin Bacon");

        HttpURLConnection conn = putRequest("addActor", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        String actorId = UUID.randomUUID().toString();
        jsonObject.put("actorId", actorId);
        jsonObject.put("name", "a1");

        conn = putRequest("addActor", jsonObject);
        assertEquals(200, conn.getResponseCode());

        String movieId = UUID.randomUUID().toString();
        jsonObject.put("movieId", movieId);
        jsonObject.put("name", "movie");

        conn = putRequest("addMovie", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        jsonObject.put("movieId", movieId);
        jsonObject.put("actorId", actorId);

        conn = putRequest("addRelationship", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        jsonObject.put("movieId", movieId);
        jsonObject.put("actorId", DBUtil.BACON_ID);

        conn = putRequest("addRelationship", jsonObject);
        assertEquals(200, conn.getResponseCode());

        conn = getRequest("computeBaconNumber?actorId=" + actorId);
        assertEquals(200, conn.getResponseCode());
    }

    public void testComputeBaconNumberFail() throws Exception {
        HttpURLConnection conn = getRequest("computeBaconNumber?actord=" + 2);
        assertEquals(400, conn.getResponseCode());

        conn = getRequest("computeBaconNumber?actorId=" + 2);
        assertEquals(404, conn.getResponseCode());
    }

    public void testComputeBaconPathPass() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("actorId", DBUtil.BACON_ID);
        jsonObject.put("name", "Kevin Bacon");

        HttpURLConnection conn = putRequest("addActor", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        String actorId = UUID.randomUUID().toString();
        jsonObject.put("actorId", actorId);
        jsonObject.put("name", "a1");

        conn = putRequest("addActor", jsonObject);
        assertEquals(200, conn.getResponseCode());

        String movieId = UUID.randomUUID().toString();
        jsonObject.put("movieId", movieId);
        jsonObject.put("name", "movie");

        conn = putRequest("addMovie", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        jsonObject.put("movieId", movieId);
        jsonObject.put("actorId", actorId);

        conn = putRequest("addRelationship", jsonObject);
        assertEquals(200, conn.getResponseCode());

        jsonObject = new JSONObject();
        jsonObject.put("movieId", movieId);
        jsonObject.put("actorId", DBUtil.BACON_ID);

        conn = putRequest("addRelationship", jsonObject);
        assertEquals(200, conn.getResponseCode());

        conn = getRequest("computeBaconPath?actorId=" + actorId);
        assertEquals(200, conn.getResponseCode());
    }

    public void testComputeBaconPathFail() throws Exception {
        HttpURLConnection conn = getRequest("computeBaconPath?actord=" + 2);
        assertEquals(400, conn.getResponseCode());

        conn = getRequest("computeBaconPath?actorId=" + 2);
        assertEquals(404, conn.getResponseCode());
    }

    public void testAddGenrePass() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("movieId", UUID.randomUUID().toString());
        jsonObject.put("name", "newMovie");
        HttpURLConnection conn = putRequest("addMovie", jsonObject);

        assertEquals(200, conn.getResponseCode());

        jsonObject.put("genre", "test");
        conn = putRequest("addGenre", jsonObject);

        assertEquals(200, conn.getResponseCode());
    }

    public void testAddGenreFail() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("genre", "test");
        HttpURLConnection conn = putRequest("addGenre", jsonObject);

        assertEquals(400, conn.getResponseCode());

        jsonObject.put("movieId", "test");
        conn = putRequest("addGenre", jsonObject);

        assertEquals(404, conn.getResponseCode());
    }

    public void testGetGenrePass() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("movieId", UUID.randomUUID().toString());
        jsonObject.put("name", "newMovie");
        HttpURLConnection conn = putRequest("addMovie", jsonObject);

        assertEquals(200, conn.getResponseCode());

        jsonObject.put("genre", "test");
        conn = putRequest("addGenre", jsonObject);

        assertEquals(200, conn.getResponseCode());

        conn = getRequest("getMoviesByGenre?genre=test");
        assertEquals(200, conn.getResponseCode());
    }

    public void testGetGenreFail() throws Exception {
        HttpURLConnection conn = getRequest("getMoviesByGenre?genr=test");
        assertEquals(400, conn.getResponseCode());

        conn = getRequest("getMoviesByGenre?genre=test");
        assertEquals(404, conn.getResponseCode());
    }
}
