package TA.java.service;
import TA.java.model.Favorite;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import data.DataConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {
    private static final String FAVORITE_DATA_DIR = DataConfig.FAVORITE_DIR;
    private ObjectMapper objectMapper = new ObjectMapper();

    public FavoriteManager() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DataConfig.ensureDir(FAVORITE_DATA_DIR);
    }

    public boolean isFavorite(String taStudentId, String jobId) {
        File[] files = new File(FAVORITE_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    Favorite favorite = objectMapper.readValue(file, Favorite.class);
                    if (favorite.getTaStudentId().equals(taStudentId) && favorite.getJobId().equals(jobId)) {
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void addFavorite(String taStudentId, String jobId) {
        if (isFavorite(taStudentId, jobId)) {
            return;
        }
        String favoriteId = "fav_" + System.currentTimeMillis();
        Favorite favorite = new Favorite(favoriteId, taStudentId, jobId, System.currentTimeMillis());
        try {
            String fileName = FAVORITE_DATA_DIR + favoriteId + ".json";
            objectMapper.writeValue(new File(fileName), favorite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeFavorite(String taStudentId, String jobId) {
        File[] files = new File(FAVORITE_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    Favorite favorite = objectMapper.readValue(file, Favorite.class);
                    if (favorite.getTaStudentId().equals(taStudentId) && favorite.getJobId().equals(jobId)) {
                        file.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Favorite> getFavoritesByTA(String taStudentId) {
        List<Favorite> result = new ArrayList<>();
        File[] files = new File(FAVORITE_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    Favorite favorite = objectMapper.readValue(file, Favorite.class);
                    if (favorite.getTaStudentId().equals(taStudentId)) {
                        result.add(favorite);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        result.sort((a, b) -> Long.compare(b.getFavoriteTime(), a.getFavoriteTime()));
        return result;
    }
}
