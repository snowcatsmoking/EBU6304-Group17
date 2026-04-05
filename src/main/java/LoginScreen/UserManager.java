package LoginScreen;

import ZiqianCao.java.TAApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

public class UserManager {

    private static final String ADMIN_AUTH_CODE = "BUPTAdmin";
    private static final String BASE_DIR = "resources/Data/";
    private static final String TA_DIR = BASE_DIR + "TAData/";
    private static final String MO_DIR = BASE_DIR + "MOData/";
    private static final String ADMIN_DIR = BASE_DIR + "AdminData/";

    private ObjectMapper objectMapper = new ObjectMapper();

    public UserManager() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        initDirectories();
    }

    private void initDirectories() {
        new File(TA_DIR).mkdirs();
        new File(MO_DIR).mkdirs();
        new File(ADMIN_DIR).mkdirs();
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

            return "SUCCESS";
        } catch (IOException e) {
            e.printStackTrace();
            return "注册失败：" + e.getMessage();
        }
    }

    public String login(String account, String password, String role) {
        if (account == null || account.trim().isEmpty()) {
            return "请输入账号";
        }
        if (password == null || password.isEmpty()) {
            return "请输入密码";
        }
        if (role == null || role.isEmpty()) {
            return "请选择登录身份";
        }

        if (role.contains("应聘者")) {
            File taFile = new File(TA_DIR + account + ".json");
            if (!taFile.exists()) {
                return "账号不存在，请先注册";
            }
            try {
                TAApplication ta = objectMapper.readValue(taFile, TAApplication.class);
                if (ta.getPassword() != null && ta.getPassword().equals(password)) {
                    return "SUCCESS:TA";
                } else {
                    return "密码错误，请重试";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "登录失败：" + e.getMessage();
            }
        } else {
            String dir = getDirectoryByRole(role);
            File userFile = new File(dir + account + ".json");

            if (!userFile.exists()) {
                return "账号不存在，请先注册";
            }

            try {
                User user = objectMapper.readValue(userFile, User.class);
                if (user.getPassword().equals(password)) {
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
}
