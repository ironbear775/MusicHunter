package com.ironbear775.musichunter;

import java.io.Serializable;

/**
 * Created by ironbear775 on 2017/12/30.
 */

public class Music implements Serializable{
    private String url;
    private String title;
    private String artist;
    private String album;
    private String albumArtUrl;
    private String musicVideoUrl;
    private String lrc;
    private String time;
    private int songID;
    private String mp3Size;
    private String mp3LowSize;
    private String oggSize;

    public String getMp3LowSize() {
        return mp3LowSize;
    }

    public int getSongID() {
        return songID;
    }

    public void setSongID(int songID) {
        this.songID = songID;
    }

    public void setMp3LowSize(String mp3LowSize) {
        this.mp3LowSize = mp3LowSize;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }


    public String getMusicVideoUrl() {
        return musicVideoUrl;
    }

    public void setMusicVideoUrl(String musicVideoUrl) {
        this.musicVideoUrl = musicVideoUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    Music(){}

    String getAlbumArtUrl() {
        return albumArtUrl;
    }

    void setAlbumArtUrl(String albumArtUrl) {
        this.albumArtUrl = albumArtUrl;
    }

    String getLrc() {
        return lrc;
    }

    void setLrc(String lrc) {
        this.lrc = lrc;
    }

    void setUrl(String url) {
        this.url = url;
    }

    String getUrl() {
        return url;
    }

    void setOggSize(String oggSize) {
        this.oggSize = oggSize;
    }

    String getOggSize() {
        return oggSize;
    }

    void setMp3Size(String mp3Size) {
        this.mp3Size = mp3Size;
    }

    String getMp3Size() {
        return mp3Size;
    }

    void setArtist(String singer) {
        this.artist = singer;
    }

    String getArtist() {
        return artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
