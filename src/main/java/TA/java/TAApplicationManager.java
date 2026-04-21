package TA.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TAApplicationManager {
    // 本地JSON存储目录路径
    private static final String STORAGE_DIR = data.DataConfig.TA_DIR;
    // Jackson JSON序列化工具
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 核心方法：创建并保存助教申请档案
     * @param application 待创建的申请对象
     * @throws ValidationException 校验失败时抛出
     * @throws IOException 文件读写失败时抛出
     */
    public void createTAApplication(TAApplication application)
            throws ValidationException, IOException {
        // 1. 校验必填字段非空
        validateRequiredFields(application);
        // 2. 校验学号唯一性
        validateStudentIdUnique(application.getTAId());
        // 3. 确保存储目录存在
        ensureStorageDirectoryExists();
        // 4. 持久化到本地JSON文件（每个TA一个文件）
        saveApplication(application);
    }

    /**
     * 校验必填字段是否为空
     */
    private void validateRequiredFields(TAApplication app) throws ValidationException {
        List<String> missingFields = new ArrayList<>();

        if (isBlank(app.getName())) missingFields.add("姓名");
        if (isBlank(app.getTAId())) missingFields.add("学号");
        if (isBlank(app.getMajor())) missingFields.add("所属专业");
        if (isBlank(app.getPhone())) missingFields.add("联系电话");
        if (isBlank(app.getEmail())) missingFields.add("邮箱");
        if (isBlank(app.getAvailableTime())) missingFields.add("可任职时间段");
        if (isBlank(app.getSkill())) missingFields.add("个人基础技能");

        if (!missingFields.isEmpty()) {
            throw new ValidationException("以下必填字段为空：" + String.join("、", missingFields));
        }
    }

    /**
     * 校验学号是否已存在（不可重复创建）
     */
    private void validateStudentIdUnique(String studentId) throws ValidationException {
        File studentFile = new File(STORAGE_DIR + studentId + ".json");
        if (studentFile.exists()) {
            throw new ValidationException("学号 " + studentId + " 已存在申请档案，不可重复创建");
        }
    }

    /**
     * 确保存储目录存在
     */
    private void ensureStorageDirectoryExists() {
        File dir = new File(STORAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 加载本地已存在的所有申请记录
     */
    private List<TAApplication> loadAllApplications() throws IOException {
        List<TAApplication> applications = new ArrayList<>();
        File dir = new File(STORAGE_DIR);
        
        if (!dir.exists() || !dir.isDirectory()) {
            return applications;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                TAApplication app = objectMapper.readValue(file, new TypeReference<TAApplication>() {});
                applications.add(app);
            }
        }
        
        return applications;
    }

    /**
     * 将申请保存到单独的JSON文件（以学号命名）
     */
    private void saveApplication(TAApplication application) throws IOException {
        String fileName = STORAGE_DIR + application.getTAId() + ".json";
        objectMapper.writeValue(new File(fileName), application);
    }

    /**
     * 工具方法：判断字符串是否为空/空白
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}

// 自定义校验异常类（用于提示业务错误）
class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
