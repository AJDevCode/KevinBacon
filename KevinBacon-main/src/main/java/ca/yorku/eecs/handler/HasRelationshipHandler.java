package ca.yorku.eecs.handler;

import ca.yorku.eecs.bean.Actor;
import ca.yorku.eecs.bean.Relationship;
import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.NotFoundException;
import ca.yorku.eecs.exception.ServerException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class HasRelationshipHandler extends RestHandler {

    public HasRelationshipHandler() {
        super(Method.GET);
    }

    @Override
    protected Response handle(JSONObject requestBodyJson) throws BadRequestException, JSONException, ServerException {
        final String actorId = requestBodyJson.getString("actorId");
        final String movieId = requestBodyJson.getString("movieId");

        if (actorId == null || actorId.isEmpty() || movieId == null || movieId.isEmpty()) {
            throw new BadRequestException("Invalid parameters");
        }

        try {
            List<Relationship> relationships = dbUtil.getRelations(actorId, movieId);
            if (relationships == null || relationships.isEmpty()) {
                requestBodyJson.put("hasRelationship", false);
            } else {
                requestBodyJson.put("hasRelationship", true);
            }

            return new Response(requestBodyJson, 200);
        } catch (NotFoundException e) {
            return new Response(404);
        }
    }
}
