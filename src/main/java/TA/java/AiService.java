package TA.java;

import com.volcengine.ark.runtime.model.files.FileMeta;
import com.volcengine.ark.runtime.model.files.UploadFileRequest;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemFile;
import com.volcengine.ark.runtime.service.ArkService;
import com.volcengine.ark.runtime.model.responses.request.*;
import com.volcengine.ark.runtime.model.responses.item.ItemEasyMessage;
import com.volcengine.ark.runtime.model.responses.constant.ResponsesConstants;
import com.volcengine.ark.runtime.model.responses.item.MessageContent;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemText;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class AiService {

    private static String MODEL = "doubao-seed-2-0-lite-260215";
    
    private final ArkService service;
    private String apiKey;

    public AiService() {
        apiKey = loadApiKey();
        loadConfig();
        
        service = ArkService.builder()
                .apiKey(apiKey)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
                .build();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
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

    public AIResponse sendMessage(String userMessage) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("请先配置 API Key");
        }

        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model(MODEL)
                .input(ResponsesInput.builder().addListItem(
                        ItemEasyMessage.builder()
                                .role(ResponsesConstants.MESSAGE_ROLE_USER)
                                .content(MessageContent.builder()
                                        .addListItem(InputContentItemText.builder()
                                                .text(userMessage)
                                                .build())
                                        .build())
                                .build()
                ).build())
                .build();
        
        ResponseObject resp = service.createResponse(request);
        return extractAIResponse(resp);
    }

    public AIResponse sendMessageWithFile(String userMessage, UploadedFile uploadedFile) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("请先配置 API Key");
        }

        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model(MODEL)
                .input(ResponsesInput.builder().addListItem(
                        ItemEasyMessage.builder()
                                .role(ResponsesConstants.MESSAGE_ROLE_USER)
                                .content(MessageContent.builder()
                                        .addListItem(InputContentItemFile.InputContentItemFileBuilder.anInputContentItemFile()
                                                .fileId(uploadedFile.id)
                                                .build())
                                        .addListItem(InputContentItemText.builder()
                                                .text(userMessage)
                                                .build())
                                        .build())
                                .build()
                ).build())
                .build();
        
        ResponseObject resp = service.createResponse(request);
        return extractAIResponse(resp);
    }

    public UploadedFile uploadFile(File file) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("请先配置 API Key");
        }

        FileMeta fileMeta = service.uploadFile(
                UploadFileRequest.builder()
                        .file(file)
                        .purpose("user_data")
                        .build());

        System.out.println("初始文件状态: " + fileMeta.getStatus());

        int maxRetries = 15;
        int retries = 0;
        
        while ("processing".equals(fileMeta.getStatus()) && retries < maxRetries) {
            System.out.println("等待文件处理... 当前状态: " + fileMeta.getStatus());
            TimeUnit.SECONDS.sleep(2);
            fileMeta = service.retrieveFile(fileMeta.getId());
            retries++;
        }

        // 只要不是错误状态，就可以继续使用
        String status = fileMeta.getStatus();
        if ("error".equals(status) || "failed".equals(status)) {
            throw new Exception("文件处理失败: " + status);
        }

        System.out.println("最终文件状态: " + status);

        return new UploadedFile(
                fileMeta.getId(),
                fileMeta.getFilename(),
                fileMeta.getBytes(),
                getFileMimeType(file.getName())
        );
    }

    private String getFileMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".avi")) return "video/x-msvideo";
        if (lower.endsWith(".mov")) return "video/quicktime";
        return "application/octet-stream";
    }

    private AIResponse extractAIResponse(ResponseObject response) {
        try {
            java.lang.reflect.Method getOutputMethod = ResponseObject.class.getMethod("getOutput");
            java.util.List<?> outputList = (java.util.List<?>) getOutputMethod.invoke(response);

            String reasoningText = null;
            String answerText = null;

            for (Object item : outputList) {
                if (item == null) continue;
                
                String className = item.getClass().getSimpleName();
                
                // 解析思考内容 - ItemReasoning
                if ("ItemReasoning".equals(className)) {
                    reasoningText = extractReasoningText(item);
                }
                
                // 解析回答内容 - ItemOutputMessage
                if ("ItemOutputMessage".equals(className)) {
                    answerText = extractAnswerText(item);
                }
            }

            return new AIResponse(reasoningText, answerText);
        } catch (Exception e) {
            e.printStackTrace();
            return new AIResponse(null, response.toString());
        }
    }

    private String extractReasoningText(Object reasoningItem) {
        try {
            java.lang.reflect.Method getSummaryMethod = reasoningItem.getClass().getMethod("getSummary");
            Object summaryResult = getSummaryMethod.invoke(reasoningItem);
            java.util.List<?> summaryList = safeToList(summaryResult);

            if (summaryList != null && !summaryList.isEmpty()) {
                Object summaryPart = summaryList.get(0);
                if (summaryPart != null) {
                    java.lang.reflect.Method getTextMethod = summaryPart.getClass().getMethod("getText");
                    String text = (String) getTextMethod.invoke(summaryPart);
                    if (text != null && !text.trim().isEmpty()) {
                        return text.trim();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractAnswerText(Object messageItem) {
        try {
            java.lang.reflect.Method getContentMethod = messageItem.getClass().getMethod("getContent");
            Object contentObj = getContentMethod.invoke(messageItem);

            if (contentObj != null) {
                java.util.List<?> contentList = safeToList(contentObj);

                if (contentList != null && !contentList.isEmpty()) {
                    for (Object contentItem : contentList) {
                        if (contentItem != null && 
                            contentItem.getClass().getSimpleName().equals("OutputContentItemText")) {
                            
                            java.lang.reflect.Method getTextMethod = contentItem.getClass().getMethod("getText");
                            String text = (String) getTextMethod.invoke(contentItem);
                            
                            if (text != null && !text.trim().isEmpty()) {
                                return text.trim();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private java.util.List<?> safeToList(Object obj) {
        if (obj == null) return null;
        
        // 如果已经是List，直接返回
        if (obj instanceof java.util.List) {
            return (java.util.List<?>) obj;
        }
        
        // 尝试调用常见的获取方法
        try {
            try {
                java.lang.reflect.Method getListMethod = obj.getClass().getMethod("getList");
                Object result = getListMethod.invoke(obj);
                if (result instanceof java.util.List) {
                    return (java.util.List<?>) result;
                }
            } catch (NoSuchMethodException e) {
                // 忽略，尝试下一个
            }
            
            try {
                java.lang.reflect.Method getItemsMethod = obj.getClass().getMethod("getItems");
                Object result = getItemsMethod.invoke(obj);
                if (result instanceof java.util.List) {
                    return (java.util.List<?>) result;
                }
            } catch (NoSuchMethodException e) {
                // 忽略，尝试下一个
            }
            
            try {
                java.lang.reflect.Method getContentMethod = obj.getClass().getMethod("getContent");
                Object result = getContentMethod.invoke(obj);
                if (result instanceof java.util.List) {
                    return (java.util.List<?>) result;
                }
            } catch (NoSuchMethodException e) {
                // 忽略
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public static class AIResponse {
        public final String reasoning;
        public final String answer;

        public AIResponse(String reasoning, String answer) {
            this.reasoning = reasoning;
            this.answer = answer;
        }
    }

    public static class UploadedFile {
        public final String id;
        public final String filename;
        public final long size;
        public final String mimeType;

        public UploadedFile(String id, String filename, long size, String mimeType) {
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
