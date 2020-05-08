package com.example.pintest1.model;

import java.util.HashMap;
import java.util.Map;

public class ContentDTO {

    public String explain;
    public String imageUrl;
    public String uid;
    public String userId;
    public String timestamp;
    public double Latitude;
    public double Longitude;
    public int favoriteCount = 0;
    public Map<String, Boolean> favorites = new HashMap<>();
    public Map<String, Comment> comments;

    public static class Comment {

        public String uid;
        public String userId;
        public String comment;
    }

    @Override
    public String toString() {
        return "uid = " + uid + " , userid = " + userId;
    }
}
