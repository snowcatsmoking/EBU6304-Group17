package TA.java;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class AiService {

    private static String API_URL = "https://ark.cn-beijing.volces.com/api/v3/responses";
    private static String UPLOAD_URL = "https://ark.cn-beijing.volces.com/api/v3/files";
    private static String MODEL = "doubao-seed-2-0-pro-260215";

    private final OkHttpClient client;
    private final Gson gson;
    private final List<Message> conversationHistory;
    private final String apiKey;

    public AiService() {
        this.apiKey = loadApiKey();
        loadConfig();
        
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.conversationHistory = new ArrayList<>();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            if (props.containsKey("ark.api.url")) {
                API_URL = props.getProperty("ark.api.url");
            }
            if (props.containsKey("ark.upload.url")) {
                UPLOAD_URL = props.getProperty("ark.upload.url");
            }
            if (props.containsKey("ark.model")) {
                MODEL = props.getProperty("ark.model");
            }
        } catch (IOException ignored) {
        }
    }

    private String loadApiKey() {
        String apiKey = System.getenv("ARK_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }

        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            apiKey = props.getProperty("ark.api.key");
            if (apiKey != null && !apiKey.isEmpty()) {
                return apiKey;
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    public String sendMessage(String userMessage) throws IOException {
        return sendMessageWithFile(userMessage, null);
    }

    public String sendMessageWithFile(String userMessage, UploadedFile file) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("请设置环境变量 ARK_API_KEY 或在 config.properties 文件中配置 ark.api.key");
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);

        JsonArray inputArray = new JsonArray();

        for (Message msg : conversationHistory) {
            inputArray.add(msg.toJson());
        }

        JsonObject userMessageItem = new JsonObject();
        userMessageItem.addProperty("type", "message");
        userMessageItem.addProperty("role", "user");

        JsonArray contentArray = new JsonArray();

        if (file != null) {
            JsonObject fileItem = new JsonObject();
            fileItem.addProperty("type", "file");
            fileItem.addProperty("file_id", file.id);
            contentArray.add(fileItem);
        }

        if (userMessage != null && !userMessage.isEmpty()) {
            JsonObject textItem = new JsonObject();
            textItem.addProperty("type", "input_text");
            textItem.addProperty("text", userMessage);
            contentArray.add(textItem);
        }

        userMessageItem.add("content", contentArray);
        inputArray.add(userMessageItem);

        conversationHistory.add(new Message("user", userMessage, file));

        requestBody.add("input", inputArray);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                throw new IOException("API请求失败: " + response.code() + " - " + response.message() + "\n" + errorBody);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            String aiResponse = parseResponse(jsonResponse);
            conversationHistory.add(new Message("assistant", aiResponse, null));
            
            return aiResponse;
        }
    }

    private String parseResponse(JsonObject jsonResponse) {
        try {
            if (jsonResponse.has("output")) {
                JsonArray outputArray = jsonResponse.getAsJsonArray("output");
                if (outputArray != null && outputArray.size() > 0) {
                    // 遍历 output 数组，找到 type = "message" 的项
                    for (int i = 0; i < outputArray.size(); i++) {
                        JsonObject outputItem = outputArray.get(i).getAsJsonObject();
                        
                        String itemType = outputItem.get("type").getAsString();
                        
                        if ("message".equals(itemType)) {
                            if (outputItem.has("content")) {
                                JsonArray contentArray = outputItem.getAsJsonArray("content");
                                if (contentArray != null && contentArray.size() > 0) {
                                    for (int j = 0; j < contentArray.size(); j++) {
                                        JsonObject contentItem = contentArray.get(j).getAsJsonObject();
                                        String contentType = contentItem.get("type").getAsString();
                                        
                                        if ("output_text".equals(contentType)) {
                                            return contentItem.get("text").getAsString();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (jsonResponse.has("choices")) {
                JsonArray choicesArray = jsonResponse.getAsJsonArray("choices");
                if (choicesArray != null && choicesArray.size() > 0) {
                    JsonObject choiceItem = choicesArray.get(0).getAsJsonObject();
                    if (choiceItem.has("message")) {
                        JsonObject messageItem = choiceItem.getAsJsonObject("message");
                        if (messageItem.has("content")) {
                            return messageItem.get("content").getAsString();
                        }
                    }
                }
            }
            
            return "无法解析响应: " + jsonResponse.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "解析响应失败: " + e.getMessage() + "\n原始响应: " + jsonResponse.toString();
        }
    }

    public UploadedFile uploadFile(File file) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("请设置环境变量 ARK_API_KEY 或在 config.properties 文件中配置 ark.api.key");
        }

        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("purpose", "user_data");

        String fileName = file.getName();
        String mimeType = getMimeType(fileName);
        RequestBody fileBody = RequestBody.create(file, MediaType.parse(mimeType));
        requestBodyBuilder.addFormDataPart("file", fileName, fileBody);

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(requestBodyBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                throw new IOException("文件上传失败: " + response.code() + " - " + response.message() + "\n" + errorBody);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            return new UploadedFile(
                    jsonResponse.get("id").getAsString(),
                    jsonResponse.get("filename").getAsString(),
                    jsonResponse.get("bytes").getAsInt(),
                    jsonResponse.get("mime_type").getAsString()
            );
        }
    }

    private String getMimeType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) return "application/pdf";
        if (lowerName.endsWith(".doc")) return "application/msword";
        if (lowerName.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lowerName.endsWith(".png")) return "image/png";
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
        if (lowerName.endsWith(".gif")) return "image/gif";
        if (lowerName.endsWith(".txt")) return "text/plain";
        if (lowerName.endsWith(".mp4")) return "video/mp4";
        if (lowerName.endsWith(".avi")) return "video/x-msvideo";
        if (lowerName.endsWith(".mov")) return "video/quicktime";
        return "application/octet-stream";
    }

    private static class Message {
        String role;
        String content;
        UploadedFile file;

        Message(String role, String content, UploadedFile file) {
            this.role = role;
            this.content = content;
            this.file = file;
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "message");
            json.addProperty("role", role);

            JsonArray contentArray = new JsonArray();

            if (file != null) {
                JsonObject fileItem = new JsonObject();
                fileItem.addProperty("type", "file");
                fileItem.addProperty("file_id", file.id);
                contentArray.add(fileItem);
            }

            if (content != null && !content.isEmpty()) {
                JsonObject textItem = new JsonObject();
                textItem.addProperty("type", "input_text");
                textItem.addProperty("text", content);
                contentArray.add(textItem);
            }

            json.add("content", contentArray);
            return json;
        }
    }

    public static class UploadedFile {
        public final String id;
        public final String filename;
        public final int size;
        public final String mimeType;

        public UploadedFile(String id, String filename, int size, String mimeType) {
            this.id = id;
            this.filename = filename;
            this.size = size;
            this.mimeType = mimeType;
        }

        @Override
        public String toString() {
            return "File: " + filename + " (" + size + " bytes)";
        }
    }
}
