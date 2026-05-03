package ai.service;

import ai.config.AIConfig;
import ai.model.AIResponse;
import ai.model.UploadedFile;
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

import com.volcengine.ark.runtime.model.responses.event.outputtext.OutputTextDeltaEvent;
import com.volcengine.ark.runtime.model.responses.event.reasoningsummary.ReasoningSummaryTextDeltaEvent;
import com.volcengine.ark.runtime.model.responses.event.response.ResponseCompletedEvent;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.lang.reflect.Method;
import java.util.List;

public class AIService {
    private final AIConfig config;
    private final ArkService service;

    public AIService() {
        this.config = new AIConfig();
        this.service = ArkService.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
    }

    public AIResponse sendMessage(String userMessage) throws Exception {
        validateConfig();

        InputContentItemText textItem = InputContentItemText.builder()
                .text(userMessage)
                .build();
        
        MessageContent content = MessageContent.builder()
                .addListItem(textItem)
                .build();
        
        ItemEasyMessage userMsg = ItemEasyMessage.builder()
                .role(ResponsesConstants.MESSAGE_ROLE_USER)
                .content(content)
                .build();
        
        ResponsesInput input = ResponsesInput.builder()
                .addListItem(userMsg)
                .build();
        
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model(config.getModel())
                .input(input)
                .build();

        ResponseObject response = service.createResponse(request);
        return extractAIResponse(response);
    }

    public AIResponse sendMessageWithFile(String userMessage, UploadedFile uploadedFile) throws Exception {
        validateConfig();

        InputContentItemFile fileItem = InputContentItemFile.InputContentItemFileBuilder
                .anInputContentItemFile()
                .fileId(uploadedFile.id)
                .build();
        
        InputContentItemText textItem = InputContentItemText.builder()
                .text(userMessage)
                .build();
        
        MessageContent content = MessageContent.builder()
                .addListItem(fileItem)
                .addListItem(textItem)
                .build();
        
        ItemEasyMessage userMsg = ItemEasyMessage.builder()
                .role(ResponsesConstants.MESSAGE_ROLE_USER)
                .content(content)
                .build();
        
        ResponsesInput input = ResponsesInput.builder()
                .addListItem(userMsg)
                .build();
        
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model(config.getModel())
                .input(input)
                .build();

        ResponseObject response = service.createResponse(request);
        return extractAIResponse(response);
    }

    public void sendMessageStream(String userMessage, Consumer<String> onReasoningDelta, Consumer<String> onAnswerDelta, Runnable onComplete) throws Exception {
        validateConfig();

        InputContentItemText textItem = InputContentItemText.builder()
                .text(userMessage)
                .build();
        
        MessageContent content = MessageContent.builder()
                .addListItem(textItem)
                .build();
        
        ItemEasyMessage userMsg = ItemEasyMessage.builder()
                .role(ResponsesConstants.MESSAGE_ROLE_USER)
                .content(content)
                .build();
        
        ResponsesInput input = ResponsesInput.builder()
                .addListItem(userMsg)
                .build();
        
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model(config.getModel())
                .stream(true)
                .input(input)
                .build();

        final StringBuilder reasoningBuilder = new StringBuilder();
        final StringBuilder answerBuilder = new StringBuilder();

        service.streamResponse(request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(event -> {
                    if (event instanceof ReasoningSummaryTextDeltaEvent) {
                        String delta = ((ReasoningSummaryTextDeltaEvent) event).getDelta();
                        if (delta != null) {
                            reasoningBuilder.append(delta);
                            if (onReasoningDelta != null) {
                                onReasoningDelta.accept(reasoningBuilder.toString());
                            }
                        }
                    }
                    if (event instanceof OutputTextDeltaEvent) {
                        String delta = ((OutputTextDeltaEvent) event).getDelta();
                        if (delta != null) {
                            answerBuilder.append(delta);
                            if (onAnswerDelta != null) {
                                onAnswerDelta.accept(answerBuilder.toString());
                            }
                        }
                    }
                    if (event instanceof ResponseCompletedEvent) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }

    public void sendMessageWithFileStream(String userMessage, UploadedFile uploadedFile, Consumer<String> onReasoningDelta, Consumer<String> onAnswerDelta, Runnable onComplete) throws Exception {
        validateConfig();

        InputContentItemFile fileItem = InputContentItemFile.InputContentItemFileBuilder
                .anInputContentItemFile()
                .fileId(uploadedFile.id)
                .build();
        
        InputContentItemText textItem = InputContentItemText.builder()
                .text(userMessage)
                .build();
        
        MessageContent content = MessageContent.builder()
                .addListItem(fileItem)
                .addListItem(textItem)
                .build();
        
        ItemEasyMessage userMsg = ItemEasyMessage.builder()
                .role(ResponsesConstants.MESSAGE_ROLE_USER)
                .content(content)
                .build();
        
        ResponsesInput input = ResponsesInput.builder()
                .addListItem(userMsg)
                .build();
        
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model(config.getModel())
                .stream(true)
                .input(input)
                .build();

        final StringBuilder reasoningBuilder = new StringBuilder();
        final StringBuilder answerBuilder = new StringBuilder();

        service.streamResponse(request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(event -> {
                    if (event instanceof ReasoningSummaryTextDeltaEvent) {
                        String delta = ((ReasoningSummaryTextDeltaEvent) event).getDelta();
                        if (delta != null) {
                            reasoningBuilder.append(delta);
                            if (onReasoningDelta != null) {
                                onReasoningDelta.accept(reasoningBuilder.toString());
                            }
                        }
                    }
                    if (event instanceof OutputTextDeltaEvent) {
                        String delta = ((OutputTextDeltaEvent) event).getDelta();
                        if (delta != null) {
                            answerBuilder.append(delta);
                            if (onAnswerDelta != null) {
                                onAnswerDelta.accept(answerBuilder.toString());
                            }
                        }
                    }
                    if (event instanceof ResponseCompletedEvent) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }

    public UploadedFile uploadFile(File file) throws Exception {
        validateConfig();

        FileMeta fileMeta = service.uploadFile(
                UploadFileRequest.builder()
                        .file(file)
                        .purpose("user_data")
                        .build()
        );

        int maxRetries = 15;
        int retries = 0;

        while ("processing".equals(fileMeta.getStatus()) && retries < maxRetries) {
            TimeUnit.SECONDS.sleep(2);
            fileMeta = service.retrieveFile(fileMeta.getId());
            retries++;
        }

        String status = fileMeta.getStatus();
        if ("error".equals(status) || "failed".equals(status)) {
            throw new Exception("文件处理失败: " + status);
        }

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

    private void validateConfig() throws Exception {
        if (!config.isValid()) {
            throw new Exception("请先配置 API Key");
        }
    }

    private AIResponse extractAIResponse(ResponseObject response) {
        try {
            Method getOutputMethod = ResponseObject.class.getMethod("getOutput");
            List<?> outputList = (List<?>) getOutputMethod.invoke(response);

            String reasoningText = null;
            String answerText = null;

            for (Object item : outputList) {
                if (item == null) continue;

                String className = item.getClass().getSimpleName();

                if ("ItemReasoning".equals(className)) {
                    reasoningText = extractReasoningText(item);
                }

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
            Method getSummaryMethod = reasoningItem.getClass().getMethod("getSummary");
            Object summaryResult = getSummaryMethod.invoke(reasoningItem);
            List<?> summaryList = safeToList(summaryResult);

            if (summaryList != null && !summaryList.isEmpty()) {
                Object summaryPart = summaryList.get(0);
                if (summaryPart != null) {
                    Method getTextMethod = summaryPart.getClass().getMethod("getText");
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
            Method getContentMethod = messageItem.getClass().getMethod("getContent");
            Object contentObj = getContentMethod.invoke(messageItem);

            if (contentObj != null) {
                List<?> contentList = safeToList(contentObj);

                if (contentList != null && !contentList.isEmpty()) {
                    for (Object contentItem : contentList) {
                        if (contentItem != null && 
                            contentItem.getClass().getSimpleName().equals("OutputContentItemText")) {

                            Method getTextMethod = contentItem.getClass().getMethod("getText");
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

    private List<?> safeToList(Object obj) {
        if (obj == null) return null;

        if (obj instanceof List) {
            return (List<?>) obj;
        }

        try {
            try {
                Method getListMethod = obj.getClass().getMethod("getList");
                Object result = getListMethod.invoke(obj);
                if (result instanceof List) {
                    return (List<?>) result;
                }
            } catch (NoSuchMethodException e) {
            }

            try {
                Method getItemsMethod = obj.getClass().getMethod("getItems");
                Object result = getItemsMethod.invoke(obj);
                if (result instanceof List) {
                    return (List<?>) result;
                }
            } catch (NoSuchMethodException e) {
            }

            try {
                Method getContentMethod = obj.getClass().getMethod("getContent");
                Object result = getContentMethod.invoke(obj);
                if (result instanceof List) {
                    return (List<?>) result;
                }
            } catch (NoSuchMethodException e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

