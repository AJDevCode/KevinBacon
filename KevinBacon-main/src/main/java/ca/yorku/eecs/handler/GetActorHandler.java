package ca.yorku.eecs.handler;

import ca.yorku.eecs.bean.Actor;
import ca.yorku.eecs.bean.Relationship;
import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.NotFoundException;
import ca.yorku.eecs.exception.ServerException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GetActorHandler extends RestHandler {

    public GetActorHandler() {
        super(Method.GET);
    }

    @Override
    protected Response handle(JSONObject requestBodyJson) throws BadRequestException, JSONException, ServerException {
        final String actorId = requestBodyJson.getString("actorId");

        if (actorId == null || actorId.isEmpty()) {
            throw new BadRequestException("Invalid parameters");
        }

        List<Actor> actorList = dbUtil.getActor(actorId);
        if (actorList == null || actorList.isEmpty()) {
            return new Response(404);
        }

        Actor actor = actorList.get(0);
        JSONObject resBody = new JSONObject();
        resBody.put("actorId", actor.getActorId());
        resBody.put("name", actor.getName());
        JSONArray movies = new JSONArray();
        resBody.put("movies", movies);
        try {
            List<Relationship> relationships = dbUtil.getRelations(actorId, null);
            if (relationships != null && !relationships.isEmpty()) {
                for (Relationship relationship : relationships) {
                    movies.put(relationship.getMovieId());
                }
            }
        } catch (NotFoundException e) {
        }

        return new Response(resBody, 200);
    }
}
