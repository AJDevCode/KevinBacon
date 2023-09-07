package ca.yorku.eecs.handler;

import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.NotFoundException;
import ca.yorku.eecs.exception.ServerException;
import org.json.JSONException;
import org.json.JSONObject;


public class ComputeBaconNumberHandler extends RestHandler {

    public ComputeBaconNumberHandler() {
        super(Method.GET);
    }

    @Override
    protected Response handle(JSONObject requestBodyJson) throws BadRequestException, JSONException, ServerException {
        final String actorId = requestBodyJson.getString("actorId");

        if (actorId == null || actorId.isEmpty()) {
            throw new BadRequestException("Invalid parameters");
        }

        try {
            int number = dbUtil.computeBaconNumber(actorId);
            if (number == -1) {
                throw new NotFoundException();
            }

            JSONObject resBody = new JSONObject();
            resBody.put("baconNumber", number);
            return new Response(resBody, 200);
        } catch (NotFoundException e) {
            return new Response(404);
        }
    }
}
