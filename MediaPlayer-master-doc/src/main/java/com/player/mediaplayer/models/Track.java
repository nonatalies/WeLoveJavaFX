package com.player.mediaplayer.models;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

/**
 * Class for information about the track .
 */
public class Track implements Serializable {
    private String songName;
    private String songArtist;
    private String songAlbum;
    private String songDuration;
    private Boolean songLiked = false;
    private String filePath;
    private byte[] songArtworkImageArray;

    /**
     * Creates an instance of the Track class.
     * @param  songName
     * @param  songAlbum
     * @param  songDuration
     * @param  filePath
     */
    public Track(String songName, String songArtist, String songAlbum, String songDuration, String filePath, byte[] songArtworkImageArray) {
        this.songName = songName;
        this.songArtist = songArtist;
        this.songAlbum = songAlbum;
        this.songDuration = songDuration;
        this.filePath = filePath;
        this.songArtworkImageArray = songArtworkImageArray;
    }
    /**
     * Returns the track name.
     * @return songName
     */
    public String getSongName() {
        return songName;
    }

    /**
     * Returns the artist of the track.
     * @return songArtist
     */
    public String getSongArtist() {
        return songArtist;
    }

    /**
     * Returns the album of the track.
     * @return
     */
    public String getSongAlbum() {
        return songAlbum;
    }

    /**
     * Returns the duration of the track.
     * @return
     */
    public String getSongDuration() {
        return songDuration;
    }

    /**
     * Returns the value of the "liked" field.
     * @return songLiked
     */
    public Boolean getSongLiked() {
        return songLiked;
    }

    /**
     * Returns the path to the track file.
     * @return filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns the album cover.
     * @return songArtworkImageArray
     */
    public Image getSongArtwork() {
        return songArtworkImageArray != null ? new Image(new ByteArrayInputStream(songArtworkImageArray)) : null;
    }
    /**
     * Sets the value of the "liked" field.
     */
    public void setSongLiked(Boolean newState) {
        songLiked = newState;
    }
}
