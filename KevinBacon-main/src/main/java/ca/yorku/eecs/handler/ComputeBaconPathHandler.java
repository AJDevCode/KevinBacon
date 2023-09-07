package ca.yorku.eecs.handler;

import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.NotFoundException;
import ca.yorku.eecs.exception.ServerException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ComputeBaconPathHandler extends RestHandler {

    public ComputeBaconPathHandler() {
        super(Method.GET);
    }

    @Override
    protected Response handle(JSONObject requestBodyJson) throws BadRequestException, JSONException, ServerException {
        final String actorId = requestBodyJson.getString("actorId");

        if (actorId == null || actorId.isEmpty()) {
            throw new BadRequestException("Invalid parameters");
        }

        try {
            List<String> path = dbUtil.computeBaconPath(actorId);
            if (path == null || path.isEmpty()) {
                throw new NotFoundException();
            }

            JSONObject resBody = new JSONObject();
            JSONArray array = new JSONArray();
            for (String s: path) {
                array.put(s);
            }
            resBody.put("baconPath", array);
            return new Response(resBody, 200);
        } catch (NotFoundException e) {
            return new Response(404);
        }
    }
}
