package data;

import LoginScreen.User;
import TA.java.TAApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据管理类。
 * 提供跨 TAData/MOData/AdminData 三个目录的统一用户查询与管理。
 * Admin 端的全局视图、账号管理功能从这里读取数据。
 *
 * 注意：
 *   - TA 的数据模型是 TAApplication（含业务档案字段）
 *   - MO/Admin 的数据模型是 User（只含账号/密码/角色）
 */
public class UserDataManager {

    private final ObjectMapper objectMapper;

    public UserDataManager() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DataConfig.ensureDir(DataConfig.TA_DIR);
        DataConfig.ensureDir(DataConfig.MO_DIR);
        DataConfig.ensureDir(DataConfig.ADMIN_DIR);
    }

    // ==================== TA 相关 ====================

    /**
     * 获取所有 TA 的个人档案列表。
     * Admin 全局视图中展示所有 TA 信息时使用。
     */
    public List<TAApplication> getAllTAs() {
        List<TAApplication> result = new ArrayList<>();
        File dir = new File(DataConfig.TA_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return result;
        for (File file : files) {
            try {
                TAApplication ta = objectMapper.readValue(file, TAApplication.class);
                result.add(ta);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 按学号查询单个 TA。
     *
     * @param studentId TA 学号
     * @return TAApplication，不存在则返回 null
     */
    public TAApplication getTAById(String studentId) {
        File file = new File(DataConfig.TA_DIR + studentId + ".json");
        if (!file.exists()) return null;
        try {
            return objectMapper.readValue(file, TAApplication.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== MO 相关 ====================

    /**
     * 获取所有 MO 的账号列表。
     */
    public List<User> getAllMOs() {
        return readUserDir(DataConfig.MO_DIR);
    }

    /**
     * 按工号查询单个 MO。
     */
    public User getMOById(String staffId) {
        return readUser(DataConfig.MO_DIR + staffId + ".json");
    }

    // ==================== Admin 相关 ====================

    /**
     * 获取所有 Admin 的账号列表。
     */
    public List<User> getAllAdmins() {
        return readUserDir(DataConfig.ADMIN_DIR);
    }

    // ==================== 账号管理（Admin 功能）====================

    /**
     * 重置指定用户的密码。
     * Admin 端"账号管理"功能使用。
     *
     * @param account  账号（学号/工号）
     * @param role     角色："TA" / "MO" / "ADMIN"
     * @param newPassword 新密码（不做格式验证，调用方负责）
     * @return true=成功，false=用户不存在
     */
    public boolean resetPassword(String account, String role, String newPassword) {
        if ("TA/java".equals(role)) {
            TAApplication ta = getTAById(account);
            if (ta == null) return false;
            ta.setPassword(newPassword);
            try {
                objectMapper.writeValue(new File(DataConfig.TA_DIR + account + ".json"), ta);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            String dir = dirByRole(role);
            if (dir == null) return false;
            User user = readUser(dir + account + ".json");
            if (user == null) return false;
            user.setPassword(newPassword);
            try {
                objectMapper.writeValue(new File(dir + account + ".json"), user);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 删除指定用户账号。
     * Admin 端"账号管理"功能使用。
     *
     * @param account 账号（学号/工号）
     * @param role    角色："TA" / "MO" / "ADMIN"
     * @return true=删除成功，false=文件不存在
     */
    public boolean deleteUser(String account, String role) {
        String dir = dirByRole(role);
        if (dir == null) return false;
        File file = new File(dir + account + ".json");
        return file.exists() && file.delete();
    }

    // ==================== 私有工具方法 ====================

    private List<User> readUserDir(String dirPath) {
        List<User> result = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return result;
        for (File file : files) {
            User user = readUser(file.getAbsolutePath());
            if (user != null) result.add(user);
        }
        return result;
    }

    private User readUser(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return null;
        try {
            return objectMapper.readValue(file, User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String dirByRole(String role) {
        switch (role) {
            case "TA/java":    return DataConfig.TA_DIR;
            case "MO":    return DataConfig.MO_DIR;
            case "ADMIN": return DataConfig.ADMIN_DIR;
            default:      return null;
        }
    }
}
