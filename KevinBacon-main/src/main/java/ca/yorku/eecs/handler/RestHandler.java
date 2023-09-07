package ca.yorku.eecs.handler;

import ca.yorku.eecs.DBUtil;
import ca.yorku.eecs.exception.BadRequestException;
import ca.yorku.eecs.exception.ServerException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class RestHandler implements HttpHandler  {
    enum Method {
        PUT, GET
    }

    protected static DBUtil dbUtil;

    protected Method method;

    public static void setDbUtil(DBUtil dbUtil) {
        RestHandler.dbUtil = dbUtil;
    }

    public RestHandler(Method method) {
        this.method = method;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (exchange.getRequestMethod().equalsIgnoreCase(method.name())) {
            try {
                JSONObject requestBodyJson = getRequestBody(exchange);
                Response response = handle(requestBodyJson);
                sendResponse(response, exchange);
            } catch (JSONException|BadRequestException e) {
                sendResponse(new Response(400), exchange);
            } catch (IOException|ServerException e) {
                sendResponse(new Response(500), exchange);
            }
        } else {
            sendResponse(new Response(500), exchange);
        }
    }

    protected abstract Response handle(JSONObject requestBodyJson) throws JSONException, BadRequestException, ServerException;

    protected static JSONObject getRequestBody(HttpExchange exchange) throws IOException, JSONException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "utf-8"));
        StringBuilder requestBodyContent = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            requestBodyContent.append(line);
        }

        if (requestBodyContent.length() > 0) {
            return new JSONObject(requestBodyContent.toString());
        }

        JSONObject jsonObject = new JSONObject();
        String[] queries = exchange.getRequestURI().getQuery().split("&");
        for (String query : queries) {
            String[] pair = query.split("=");
            jsonObject.put(pair[0], pair[1]);
        }

        return jsonObject;
    }

    protected static void sendResponse(Response response, HttpExchange exchange) {
        System.out.println("send response: " + response.toString());
        try {
            int code = response.getCode();
            if (code != 200) {
                exchange.sendResponseHeaders(code, 0);
            } else {
                OutputStream out = exchange.getResponseBody();
                if (response.getResBody() != null) {
                    exchange.getResponseHeaders().add("Content-Type:", "application/json");
                    byte[] responseBytes = response.getResBody().toString().getBytes();
                    exchange.sendResponseHeaders(code, responseBytes.length);

                    out.write(responseBytes);
                } else {
                    exchange.sendResponseHeaders(code, 0);
                }

                out.flush();
                out.close();
            }
        } catch (IOException e) {
            System.out.println("send response failed " + e.getMessage());
        }
    }

    protected static class Response {
        JSONObject resBody;
        int code;

        public Response(JSONObject resBody, int code) {
            this.resBody = resBody;
            this.code = code;
        }

        public Response(int code) {
            this.code = code;
        }

        public JSONObject getResBody() {
            return resBody;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "resBody=" + resBody +
                    ", code=" + code +
                    '}';
        }
    }
}
