package com.mefrp.frpmc.api;

import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MefrpApiClient {

    private final String baseUrl;
    private final String userAgent;
    private final int timeout;
    private volatile String authToken;
    private volatile boolean connected = false;

    private final OkHttpClient client;
    private final MediaType JSON_MEDIA_TYPE;

    public MefrpApiClient(String baseUrl, String userAgent, int timeout) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.timeout = timeout;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request.Builder builder = original.newBuilder()
                            .header("User-Agent", userAgent)
                            .header("Content-Type", "application/json");
                    String token = authToken; // Capture volatile field
                    if (token != null && !token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .build();
        this.JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    }

    // Authentication
    public ApiResponse login(String username, String password) {
        return login(username, password, null);
    }

    public ApiResponse login(String username, String password, String captchaToken) {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        if (captchaToken != null && !captchaToken.isEmpty()) {
            body.put("captchaToken", captchaToken);
        }
        
        ApiResponse response = post("/public/login", body.toString());
        if (response.isSuccess()) {
            JSONObject data = response.getJson();
            if (data.has("token")) {
                this.authToken = data.getString("token");
                this.connected = true;
            }
        }
        return response;
    }

    public ApiResponse register(String username, String password, String email) {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        body.put("email", email);
        
        return post("/public/register", body.toString());
    }

    public ApiResponse getUserInfo() {
        return get("/auth/user/info");
    }

    public ApiResponse signIn() {
        return post("/auth/user/sign", "{}");
    }

    // Tunnel Management
    public ApiResponse createTunnel(String name, String nodeId, String localIp, int localPort, String remotePort, String tunnelType) {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("node_id", nodeId);
        body.put("local_ip", localIp);
        body.put("local_port", localPort);
        body.put("remote_port", remotePort);
        body.put("tunnel_type", tunnelType);
        
        return post("/auth/proxy/create", body.toString());
    }

    public ApiResponse listTunnels() {
        return get("/auth/proxy/list");
    }

    public ApiResponse deleteTunnel(String tunnelId) {
        return delete("/auth/proxy/delete/" + tunnelId);
    }

    public ApiResponse getTunnelStatus(String tunnelId) {
        return get("/auth/proxy/status/" + tunnelId);
    }

    public ApiResponse startTunnel(String tunnelId) {
        return post("/auth/proxy/start/" + tunnelId, "{}");
    }

    public ApiResponse stopTunnel(String tunnelId) {
        return post("/auth/proxy/stop/" + tunnelId, "{}");
    }

    // Node Management
    public ApiResponse getNodeList() {
        return get("/auth/node/list");
    }

    public ApiResponse getNodeStatus(String nodeId) {
        return get("/auth/node/status/" + nodeId);
    }

    // System
    public ApiResponse getSystemStatus() {
        return get("/auth/system/status");
    }

    public ApiResponse getPublicStats() {
        return get("/public/stats");
    }

    // HTTP Methods
    private ApiResponse get(String endpoint) {
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .get()
                .build();
        return execute(request);
    }

    private ApiResponse post(String endpoint, String jsonBody) {
        RequestBody body = RequestBody.create(jsonBody, JSON_MEDIA_TYPE);
        
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .post(body)
                .build();
        return execute(request);
    }

    private ApiResponse delete(String endpoint) {
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .delete()
                .build();
        return execute(request);
    }

    private ApiResponse execute(Request request) {
        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            int code = response.code();
            boolean success = code >= 200 && code < 300;
            
            JSONObject json = null;
            if (body.startsWith("<")) {
                // HTML response from WAF - blocked
                return new ApiResponse(false, code, null, "Request blocked by WAF. Check User-Agent.");
            }
            
            try {
                json = new JSONObject(body);
            } catch (Exception e) {
                // Not JSON
            }
            
            String message = json != null ? json.optString("message", "") : body;
            return new ApiResponse(success, code, json, message);
            
        } catch (IOException e) {
            return new ApiResponse(false, -1, null, e.getMessage());
        }
    }

    public synchronized void disconnect() {
        this.authToken = null;
        this.connected = false;
    }

    public boolean isConnected() {
        String token = authToken; // Capture volatile
        return connected && token != null && !token.isEmpty();
    }

    public boolean hasToken() {
        String token = authToken; // Capture volatile
        return token != null && !token.isEmpty();
    }

    public synchronized void setToken(String token) {
        this.authToken = token;
        this.connected = token != null && !token.isEmpty();
    }

    public String getToken() {
        return authToken;
    }

    // Inner class for API responses
    public static class ApiResponse {
        private final boolean success;
        private final int code;
        private final JSONObject json;
        private final String message;

        public ApiResponse(boolean success, int code, JSONObject json, String message) {
            this.success = success;
            this.code = code;
            this.json = json;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getCode() {
            return code;
        }

        public JSONObject getJson() {
            return json;
        }

        public String getMessage() {
            return message;
        }

        public String getString(String key) {
            return json != null ? json.optString(key, "") : "";
        }

        public JSONArray getArray(String key) {
            return json != null ? json.optJSONArray(key) : null;
        }
    }
}