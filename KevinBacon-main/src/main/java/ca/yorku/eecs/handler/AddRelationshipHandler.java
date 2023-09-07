package ca.yorku.eecs.handler;

import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.NotFoundException;
import ca.yorku.eecs.exception.ServerException;
import ca.yorku.eecs.handler.RestHandler.Method;
import ca.yorku.eecs.handler.RestHandler.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class AddRelationshipHandler extends RestHandler  {

    public AddRelationshipHandler() {
        super(Method.PUT);
    }

    @Override
    protected Response handle(JSONObject requestBodyJson) throws JSONException, BadRequestException, ServerException {
        final String actorId = requestBodyJson.getString("actorId");
        final String movieId = requestBodyJson.getString("movieId");

        if (actorId == null || movieId == null || actorId.isEmpty() || movieId.isEmpty()) {
            throw new BadRequestException("Invalid parameters");
        }

        try {
            if (!dbUtil.addRelationship(actorId, movieId)) {
                throw new ServerException();
            }
        } catch (NotFoundException e) {
            return new Response(404);
        }

        return new Response(200);
    }
}
