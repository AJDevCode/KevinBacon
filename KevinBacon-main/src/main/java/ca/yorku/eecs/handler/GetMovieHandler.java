package ca.yorku.eecs.handler;

import ca.yorku.eecs.bean.Actor;
import ca.yorku.eecs.bean.Movie;
import ca.yorku.eecs.bean.Relationship;
import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.NotFoundException;
import ca.yorku.eecs.exception.ServerException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GetMovieHandler extends RestHandler {

    public GetMovieHandler() {
        super(Method.GET);
    }

    @Override
    protected Response handle(JSONObject requestBodyJson) throws BadRequestException, JSONException, ServerException {
        final String movieId = requestBodyJson.getString("movieId");

        if (movieId == null || movieId.isEmpty()) {
            throw new BadRequestException("Invalid parameters");
        }

        List<Movie> movieList = dbUtil.getMovie(movieId);
        if (movieList == null || movieList.isEmpty()) {
            return new Response(404);
        }

        Movie movie = movieList.get(0);
        JSONObject resBody = new JSONObject();
        resBody.put("movieId", movie.getMovieId());
        resBody.put("name", movie.getName());
        JSONArray actors = new JSONArray();
        resBody.put("actors", actors);
        try {
            List<Relationship> relationships = dbUtil.getRelations(null, movieId);
            if (relationships != null && !relationships.isEmpty()) {
                for (Relationship relationship : relationships) {
                    actors.put(relationship.getActorId());
                }
            }
        } catch (NotFoundException e) {
        }

        return new Response(resBody, 200);
    }
}
