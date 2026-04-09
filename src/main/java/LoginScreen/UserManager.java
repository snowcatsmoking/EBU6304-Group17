package LoginScreen;

import ZiqianCao.java.TAApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import data.DataConfig;
import data.LogManager;

import java.io.File;
import java.io.IOException;

public class UserManager {

    private static final String ADMIN_AUTH_CODE = "BUPTAdmin";
    private static final String TA_DIR    = DataConfig.TA_DIR;
    private static final String MO_DIR    = DataConfig.MO_DIR;
    private static final String ADMIN_DIR = DataConfig.ADMIN_DIR;

    private ObjectMapper objectMapper = new ObjectMapper();
    private LogManager logManager = new LogManager();

    public UserManager() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        initDirectories();
    }

    private void initDirectories() {
        DataConfig.initAllDirs();
    }

    private String getDirectoryByRole(String role) {
        if (role.contains("应聘者")) {
            return TA_DIR;
        } else if (role.contains("课程组织者")) {
            return MO_DIR;
        } else if (role.contains("系统管理员")) {
            return ADMIN_DIR;
        }
        return TA_DIR;
    }

    private String getRoleKey(String role) {
        if (role.contains("应聘者")) {
            return "TA";
        } else if (role.contains("课程组织者")) {
            return "MO";
        } else if (role.contains("系统管理员")) {
            return "ADMIN";
        }
        return "TA";
    }

    public String register(String account, String password, String role, String authCode) {
        if (account == null || account.trim().isEmpty()) {
            return "请输入账号";
        }
        if (password == null || password.isEmpty()) {
            return "请输入密码";
        }
        if (password.length() < 6) {
            return "密码至少需要6位";
        }
        if (role == null || role.isEmpty()) {
            return "请选择角色";
        }

        if (role.contains("系统管理员")) {
            if (authCode == null || !authCode.equals(ADMIN_AUTH_CODE)) {
                return "管理员授权码错误";
            }
        }

        String dir = getDirectoryByRole(role);
        File userFile = new File(dir + account + ".json");

        if (userFile.exists()) {
            return "该账号已注册";
        }

        User user = new User(account, password, getRoleKey(role));
        try {
            objectMapper.writeValue(userFile, user);

            if (role.contains("应聘者")) {
                TAApplication taProfile = new TAApplication("", account, "", "", "", "", "");
                taProfile.setPassword(password);
                File taFile = new File(TA_DIR + account + ".json");
                objectMapper.writeValue(taFile, taProfile);
            }

            logManager.log(account, "注册账号", account, "角色: " + getRoleKey(role));
            return "SUCCESS";
        } catch (IOException e) {
            e.printStackTrace();
            return "注册失败：" + e.getMessage();
        }
    }

    public String login(String account, String password) {
        if (account == null || account.trim().isEmpty()) {
            return "请输入账号";
        }
        if (password == null || password.isEmpty()) {
            return "请输入密码";
        }

        // Try TA first
        File taFile = new File(TA_DIR + account + ".json");
        if (taFile.exists()) {
            try {
                TAApplication ta = objectMapper.readValue(taFile, TAApplication.class);
                if (ta.getPassword() != null && ta.getPassword().equals(password)) {
                    logManager.log(account, "登录", account, "角色: TA");
                    return "SUCCESS:TA";
                } else {
                    return "密码错误，请重试";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "登录失败：" + e.getMessage();
            }
        }

        // Try MO and Admin directories
        for (String dir : new String[]{MO_DIR, ADMIN_DIR}) {
            File userFile = new File(dir + account + ".json");
            if (userFile.exists()) {
                try {
                    User user = objectMapper.readValue(userFile, User.class);
                    if (user.getPassword().equals(password)) {
                        logManager.log(account, "登录", account, "角色: " + user.getRole());
                        return "SUCCESS:" + user.getRole();
                    } else {
                        return "密码错误，请重试";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "登录失败：" + e.getMessage();
                }
            }
        }

        return "账号不存在，请先注册";
    }
}
