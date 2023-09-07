package ca.yorku.eecs.handler;

import ca.yorku.eecs.bean.Movie;
import ca.yorku.eecs.bean.Relationship;
import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.NotFoundException;
import ca.yorku.eecs.exception.ServerException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GetMovieByGenreHandler extends RestHandler {

    public GetMovieByGenreHandler() {
        super(Method.GET);
    }

    @Override
    protected Response handle(JSONObject requestBodyJson) throws BadRequestException, JSONException, ServerException {
        final String genre = requestBodyJson.getString("genre");

        if (genre == null || genre.isEmpty()) {
            throw new BadRequestException("Invalid parameters");
        }

        List<Movie> movieList = dbUtil.getMovieByGenre(genre);
        if (movieList == null || movieList.isEmpty()) {
            return new Response(404);
        }

        JSONObject resBody = new JSONObject();
        JSONArray movies = new JSONArray();
        resBody.put("genre", genre);
        resBody.put("movies", movies);
        for (Movie movie: movieList) {
            movies.put(movie.getMovieId());
        }

        return new Response(resBody, 200);
    }
}
