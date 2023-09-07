package ca.yorku.eecs.handler;

import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.ServerException;
import org.json.JSONException;
import org.json.JSONObject;


public class AddActorHandler extends RestHandler {

    public AddActorHandler() {
        super(Method.PUT);
    }

    @Override
    protected Response handle(JSONObject requestBodyJson) throws BadRequestException, JSONException, ServerException {
        final String name = requestBodyJson.getString("name");
        final String actorId = requestBodyJson.getString("actorId");

        if (name == null || actorId == null || name.isEmpty() || actorId.isEmpty()) {
            throw new BadRequestException("Invalid parameters");
        }

        if (!dbUtil.addActor(name, actorId)) {
            throw new ServerException();
        }

        return new Response(200);
    }
}
