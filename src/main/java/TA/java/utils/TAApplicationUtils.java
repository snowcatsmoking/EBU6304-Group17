package TA.java.utils;

import TA.java.TAApplication;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class TAApplicationUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean checkProfileComplete(TAApplication user) {
        if (user == null) {
            return false;
        }
        return user.getName() != null && !user.getName().trim().isEmpty()
            && user.getMajor() != null && !user.getMajor().trim().isEmpty()
            && user.getPhone() != null && !user.getPhone().trim().isEmpty()
            && user.getAvailableTime() != null && !user.getAvailableTime().trim().isEmpty()
            && user.getSkill() != null && !user.getSkill().trim().isEmpty();
    }

    public static boolean checkProfileComplete(String studentId) {
        try {
            TAApplication user = loadUser(studentId);
            return checkProfileComplete(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static TAApplication loadUser(String studentId) {
        try {
            File file = new File(data.DataConfig.TA_DIR + studentId + ".json");
            if (file.exists()) {
                return objectMapper.readValue(file, TAApplication.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new TAApplication("Unknown User", studentId, "Unknown Major", "", "", "", "");
    }
}

