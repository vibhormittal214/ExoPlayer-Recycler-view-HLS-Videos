package com.example.myapplication;

public class MediaObject {
    private String media_url;
    private String title;
    private String thumbnail;
    public MediaObject(String title, String media_url,String thumbnail) {
        this.title = title;
        this.media_url = media_url;
        this.thumbnail = thumbnail;


    }
    public MediaObject() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMedia_url() {
        return media_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }
    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
