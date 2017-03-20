package com.myboxteam.scanner.dto;

/**
 * Created by jack on 5/1/16.
 */
public class Video extends BaseDto {

    private String objectId;
    private String title;
    private String banner;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

}
