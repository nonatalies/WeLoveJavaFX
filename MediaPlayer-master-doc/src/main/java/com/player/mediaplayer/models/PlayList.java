package com.player.mediaplayer.models;

import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Class Playlist for storing information about it and the track list.
 */
public class PlayList {
    /**
     * A special class for saving/loading a playlist in JSON format.
     */
    public static class JsonPlayList {
        public JsonPlayList(PlayList playlist) {
            this.name = playlist.getName().get();
            this.playList = playlist.getPlayList().stream().map(item -> item.getFilePath()).toList();
        }
        public String name;
        public List<String> playList;
    }
    private SimpleStringProperty name = new SimpleStringProperty("");
    private int currentTrackID;
    private ArrayList<Track> playList;

    /**
     * Creates an instance of the PlayList class.
     * @param  name
     * @param  playList
     */
    public PlayList(String name, ArrayList<Track> playList) {
        this.name.set(name);
        this.playList = playList;
        if (this.playList == null) {
            this.playList = new ArrayList<>();
        }
        this.currentTrackID = -1;
    }

    /**
     * Returns a property that stores the playlist name.
     * @return name
     */
    public SimpleStringProperty getName() {
        return name;
    }

    /**
     * Sets a new playlist name.
     * @param  name
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * Returns a list of tracks.
     * @return playList
     */
    public ArrayList<Track> getPlayList() {
        return playList;
    }

    /**
     * Replaces the current playlist with a playList.
     *  @param  playList
     */
    public void setPlayList(ArrayList<Track> playList) {
        this.playList = playList;
    }

    /**
     * Returns the ID of the current track.
     * @return currentTrackID
     */
    public int getCurrentTrackID() {
        return currentTrackID;
    }

    /**
     * Sets the ID of the current track.
     * @param  currentTrackID
     */
    public void setCurrentTrackID(int currentTrackID) {
        this.currentTrackID = currentTrackID;
    }
}
