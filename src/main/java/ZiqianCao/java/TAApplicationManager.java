package ZiqianCao.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TAApplicationManager {
    // 本地JSON存储文件路径
    private static final String STORAGE_FILE = "resources/Data/TAData/ta_applications.json";
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
        // 3. 持久化到本地JSON文件
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
    private void validateStudentIdUnique(String studentId) throws IOException, ValidationException {
        List<TAApplication> existingApps = loadAllApplications();
        boolean isDuplicate = existingApps.stream()
                .anyMatch(app -> studentId.equals(app.getTAId()));
        if (isDuplicate) {
            throw new ValidationException("学号 " + studentId + " 已存在申请档案，不可重复创建");
        }
    }

    /**
     * 加载本地已存在的所有申请记录
     */
    private List<TAApplication> loadAllApplications() throws IOException {
        File file = new File(STORAGE_FILE);
        if (!file.exists()) {
            return new ArrayList<>(); // 文件不存在时返回空列表
        }
        return objectMapper.readValue(file, new TypeReference<List<TAApplication>>() {});
    }

    /**
     * 将新申请追加保存到JSON文件
     */
    private void saveApplication(TAApplication application) throws IOException {
        List<TAApplication> existingApps = loadAllApplications();
        existingApps.add(application);
        objectMapper.writeValue(new File(STORAGE_FILE), existingApps);
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
