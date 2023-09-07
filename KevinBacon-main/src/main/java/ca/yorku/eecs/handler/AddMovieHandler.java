package ca.yorku.eecs.handler;

import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.ServerException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class AddMovieHandler extends RestHandler {

    public AddMovieHandler() {
        super(Method.PUT);
    }

    @Override
    protected Response handle(JSONObject requestBodyJson) throws JSONException, BadRequestException, ServerException {
        final String name = requestBodyJson.getString("name");
        final String movieId = requestBodyJson.getString("movieId");

        if (name == null || movieId == null || name.isEmpty() || movieId.isEmpty()) {
            throw new BadRequestException("Invalid parameters");
        }

        if (!dbUtil.addMovie(name, movieId)) {
            throw new ServerException();
        }

        return new Response(200);
    }
}
