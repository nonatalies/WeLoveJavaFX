package com.player.mediaplayer;

import com.player.mediaplayer.models.PlayList;
import com.player.mediaplayer.models.Player;
import com.player.mediaplayer.models.Track;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Class PlayerContext for storing references to global objects.
 */
public class PlayerContext {
    public static Timer globalTimer = null;
    public static ObservableList<Track> selectedPlaylist;
    public static List<Track> selectedPlaylistRef = null;
    public static SimpleStringProperty selectedPlaylistName = new SimpleStringProperty("All tracks");
    public final static Player player = new Player();
    private PlayerContext() {}
}
