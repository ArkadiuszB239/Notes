package com.example.notes.main.gallery;

import com.google.firebase.database.Exclude;

public class Upload {

    private String content;

    private String type;

    private String key;


    public Upload() {}

    public Upload(String content, String type) {
        content = content;
        type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public String getKey() {
        return key;
    }
}
