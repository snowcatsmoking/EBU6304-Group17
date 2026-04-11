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
        if (role.contains("TA Applicant")) {
            return TA_DIR;
        } else if (role.contains("Module Organiser")) {
            return MO_DIR;
        } else if (role.contains("Admin")) {
            return ADMIN_DIR;
        }
        return TA_DIR;
    }

    private String getRoleKey(String role) {
        if (role.contains("TA Applicant")) {
            return "TA";
        } else if (role.contains("Module Organiser")) {
            return "MO";
        } else if (role.contains("Admin")) {
            return "ADMIN";
        }
        return "TA";
    }

    public String register(String account, String password, String role, String authCode) {
        if (account == null || account.trim().isEmpty()) {
            return "Please enter your account";
        }
        if (password == null || password.isEmpty()) {
            return "Please enter your password";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        if (role == null || role.isEmpty()) {
            return "Please select a role";
        }

        if (role.contains("Admin")) {
            if (authCode == null || !authCode.equals(ADMIN_AUTH_CODE)) {
                return "Incorrect admin authorisation code";
            }
        }

        String dir = getDirectoryByRole(role);
        File userFile = new File(dir + account + ".json");

        if (userFile.exists()) {
            return "This account is already registered";
        }

        User user = new User(account, password, getRoleKey(role));
        try {
            objectMapper.writeValue(userFile, user);

            if (role.contains("TA Applicant")) {
                TAApplication taProfile = new TAApplication("", account, "", "", "", "", "");
                taProfile.setPassword(password);
                File taFile = new File(TA_DIR + account + ".json");
                objectMapper.writeValue(taFile, taProfile);
            }

            logManager.log(account, "Register", account, "Role: " + getRoleKey(role));
            return "SUCCESS";
        } catch (IOException e) {
            e.printStackTrace();
            return "Registration failed: " + e.getMessage();
        }
    }

    public String login(String account, String password) {
        if (account == null || account.trim().isEmpty()) {
            return "Please enter your account";
        }
        if (password == null || password.isEmpty()) {
            return "Please enter your password";
        }

        // Try TA first
        File taFile = new File(TA_DIR + account + ".json");
        if (taFile.exists()) {
            try {
                TAApplication ta = objectMapper.readValue(taFile, TAApplication.class);
                if (ta.getPassword() != null && ta.getPassword().equals(password)) {
                    logManager.log(account, "Login", account, "Role: TA");
                    return "SUCCESS:TA";
                } else {
                    return "Incorrect password, please try again";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Login failed: " + e.getMessage();
            }
        }

        // Try MO and Admin directories
        for (String dir : new String[]{MO_DIR, ADMIN_DIR}) {
            File userFile = new File(dir + account + ".json");
            if (userFile.exists()) {
                try {
                    User user = objectMapper.readValue(userFile, User.class);
                    if (user.getPassword().equals(password)) {
                        logManager.log(account, "Login", account, "Role: " + user.getRole());
                        return "SUCCESS:" + user.getRole();
                    } else {
                        return "Incorrect password, please try again";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Login failed: " + e.getMessage();
                }
            }
        }

        return "Account not found, please register first";
    }
}
