package com.player.mediaplayer.models;

import com.player.mediaplayer.PlayerContext;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Class Player responsible for playing music.
 * Is a wrapper over JavaFX MediaPlayer, implementing an add. logic.
 */
public class Player {
    private final int PLAY_PREVIOUS_THRESHOLD = 3;
    private final String APP_DATA_NAME = "data";
    private ObservableList<Track> allTracks;
    private ObservableList<Track> currentPlayList;
    private ObservableList<PlayList> playLists;
    private Queue<Track> queue = new LinkedList<>();
    private Runnable onEndOfMediaRunnable = null;
    private Runnable onPause = null;
    private Runnable onPlay = null;
    private int currentTrackID;
    private SimpleObjectProperty<Track> currentTrack;
    private SimpleDoubleProperty currentVolume;
    private SimpleBooleanProperty isShuffling;
    private SimpleBooleanProperty isRepeating;
    private SimpleBooleanProperty onlyFavorites;
    private SimpleObjectProperty<Predicate<Track>> currentTrackFilter;
    private PlayerState state = null;
    private MediaPlayer mediaPlayer = null;

    /**
     * Creates an instance of the Player class.
     */
    public Player() {
        this.allTracks = FXCollections.observableArrayList();
        this.currentPlayList = FXCollections.observableArrayList();
        this.currentTrackID = -1;
        this.currentVolume = new SimpleDoubleProperty(0.5);
        this.isShuffling = new SimpleBooleanProperty(false);
        this.isRepeating = new SimpleBooleanProperty(false);
        this.onlyFavorites = new SimpleBooleanProperty(false);
        this.currentTrackFilter = new SimpleObjectProperty<>(track -> true);
        this.currentTrack = new SimpleObjectProperty<>();
        this.onlyFavorites.addListener((observableValue, aBoolean, t1) -> filterPlayList());
        this.currentTrackFilter.addListener((observableValue, trackPredicate, t1) -> filterPlayList());
        this.playLists = FXCollections.observableArrayList();
        loadState();
    }

    /**
     * Returns the loaded state.
     * @return
     */
    public PlayerState getLoadedState() {
        return state;
    }

    /**
     * Performs deserialization of the player state from a file.
     */
    private void loadState() {
        try
        {
            FileInputStream file = new FileInputStream(APP_DATA_NAME);
            ObjectInputStream in = new ObjectInputStream(file);
            PlayerState state = (PlayerState) in.readObject();
            in.close();
            file.close();
            this.state = state;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }
    }

    /**
     * Applies the loaded state to the player.
     */
    public void applyState() {
        if (state != null) {
            state.initPlayer(this);
            pause();
        }
    }

    /**
     * Serializes the current state of the player.
     */
    public void saveState() {
        try {
            FileOutputStream file = new FileOutputStream(APP_DATA_NAME);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(new PlayerState(this));
            out.close();
            file.close();
        } catch(IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Filters the playlist according to the specified template.
     * Used when searching, as well as when displaying "only liked" tracks.
     */
    private void filterPlayList() {
        FilteredList<Track> searchedTracks = new FilteredList(allTracks);
        searchedTracks.setPredicate(track -> onlyFavorites.get() ? track.getSongLiked() && currentTrackFilter.get().test(track) : currentTrackFilter.get().test(track));
        PlayerContext.selectedPlaylist.clear();
        PlayerContext.selectedPlaylist.setAll(searchedTracks);
    }

    /**
     * Returns the current playlist.
     * @return
     */
    public ObservableList<Track> getCurrentPlayList() {
        return currentPlayList;
    }

    /**
     * Adds a new track to the list of all tracks and to the current playlist.
     * @param mp3Track
     */
    public void addTrack(Track mp3Track) {
        allTracks.add(mp3Track);
        if (PlayerContext.selectedPlaylistName.get() == "All tracks") {
            PlayerContext.selectedPlaylist.add(mp3Track);
        }
    }

    /**
     * Replaces the current playlist with tracks.
     * @param tracks
     */
    public void setCurrentPlayList(List<Track> tracks) {
        currentPlayList.clear();
        currentPlayList.setAll(tracks);
        currentTrackID = 0;
    }

    /**
     * Returns a list of all tracks.
     * @return
     */
    public ObservableList<Track> getAllTracks() { return allTracks;
    }

    /**
     * Returns the isShuffling property responsible for mixed playback.
     * @return
     */
    public SimpleBooleanProperty getIsShuffling() { return isShuffling;
    }

    /**
     * Updates the values of the isShuffling property.
     * @param  isShuffling
     */
    public void setIsShuffling(Boolean isShuffling) { this.isShuffling.set(isShuffling);
    }

    /**
     * Returns the isRepeating property responsible for repeat playback.
     * @return
     */
    public SimpleBooleanProperty getIsRepeating() { return isRepeating;
    }

    /**
     * Обновляет значения свойства isRepeating.
     * @param isRepeating
     */
    public void setIsRepeating(Boolean isRepeating) {
        this.isRepeating.set(isRepeating);
    }

    /**
     * Returns a property that stores the current volume value.
     * @return
     */
    public SimpleDoubleProperty getCurrentVolume() {
        return currentVolume;
    }

    /**
     * Sets a new volume value.
     * @param volume
     */
    public void setCurrentVolume(double volume) {
        currentVolume.set(volume);
    }

    /**
     * Sets the playlist filtering algorithm (used when searching).
     * @param callable
     */
    public void setCurrentTrackFilter(Predicate<Track> callable) {
        currentTrackFilter.set(callable);
    }

    /**
     * Returns the onlyFavorites property, which is responsible for filtering "only liked" tracks.
     */
    public SimpleBooleanProperty getOnlyFavorites() {
        return this.onlyFavorites;
    }

    /**
     * Обновляет значения свойства onlyFavorites.
     */
    public void setOnlyFavorites(Boolean onlyFavorites) {
        this.onlyFavorites.set(onlyFavorites);
    }

    /**
     * Starts playback of the current track.
     */
    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        if (currentTrackID == -1) {
            throw new IllegalStateException("Nothing to play");
        }
        Track track = currentTrack.get();
        Media media = new Media(track.getFilePath());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(currentVolume.get());
        if (onEndOfMediaRunnable == null) {
            throw new IllegalStateException("On end of media action is not set up");
        }
        mediaPlayer.setOnEndOfMedia(onEndOfMediaRunnable);
        mediaPlayer.setOnPlaying(onPlay);
        mediaPlayer.setOnPaused(onPause);
        mediaPlayer.play();
    }

    /**
     * Pauses playback of the current track.
     */
    public void pause() {
        if (mediaPlayer == null) {
            throw new IllegalStateException("MediaPlayer does not exist");
        }
        mediaPlayer.pause();
    }

    /**
     * Resumes playback of the current track.
     */
    public void resume() {
        if (mediaPlayer == null) {
            throw new IllegalStateException("MediaPlayer does not exist");
        }
        mediaPlayer.play();
    }

    /**
     * Jumps to the previous track or plays the current track first.
     * If the current track is played for more than PLAY_PREVIOUS_THRESHOLD seconds, then playback starts over, otherwise the previous track is played.
     * When the beginning of the playlist is reached, the transition to the previous track is not possible.
     */
    public Boolean previous() {
        if (mediaPlayer == null) {
            throw new IllegalStateException("MediaPlayer does not exist");
        }
        if (!queue.isEmpty()) {
            return false;
        }
        if (mediaPlayer.getCurrentTime().toSeconds() < PLAY_PREVIOUS_THRESHOLD && currentTrackID > 0) {
            --currentTrackID;
            currentTrack.set(currentPlayList.get(currentTrackID));
            return true;
        }
        return false;
    }

    /**
     * Searches for the next track to be played.
     * Takes into account the state of the queue, the properties of repetition and playback mixed.
     */
    private void findNextTrack() {
        if (isRepeating.get()) {
            return;
        }

        int nextTrackID = currentTrackID;
        if (isShuffling.get() && queue.isEmpty()) {
            if (currentPlayList.size() < 2) {
                return;
            }
            do {
                nextTrackID = ThreadLocalRandom.current().nextInt(0, currentPlayList.size());
            } while (nextTrackID == currentTrackID);
        } else {
            if (!queue.isEmpty()) {
                currentTrack.set(queue.poll());
                return;
            }
            if (currentTrackID >= currentPlayList.size() - 1) {
                nextTrackID = 0;
            } else {
                ++nextTrackID;
            }
        }
        currentTrackID = nextTrackID;
        currentTrack.set(currentPlayList.get(currentTrackID));
    }

    /**
     * Jumps to the next track.
     */
    public void next() {
        if (mediaPlayer == null) {
            throw new IllegalStateException("MediaPlayer does not exist");
        }
        findNextTrack();
        play();
    }

    /**
     * Returns the internal JavaFX MediaPlayer.
     */
    public MediaPlayer getMediaPlayer() {
        if (mediaPlayer == null) {
            throw new IllegalStateException("MediaPlayer does not exist");
        }
        return mediaPlayer;
    }

    /**
     * Returns a property that stores the current track.

     */
    public SimpleObjectProperty<Track> getCurrentTrack() {
        if (currentTrack == null) {
            throw new IllegalStateException("No track");
        }
        return currentTrack;
    }

    /**
     * Sets the current track.
     * @param  track
     */
    public void setCurrentTrack(Track track) {
        currentTrack.set(track);
    }

    /**
     * Specifies the action to be performed at the moment when the track playback ends.
     * @param  runnable
     */
    public void setOnEndOfMedia(Runnable runnable) {
        onEndOfMediaRunnable = runnable;
    }

    /**
     * Specifies the action to be performed when playback starts.
     * @param onPlay
     */
    public void setOnPlay(Runnable onPlay) {
        this.onPlay = onPlay;
    }
    /**
     * Specifies the action to be performed when playback is paused.
     * @param onPause
     */
    public void setOnPause(Runnable onPause) {
        this.onPause = onPause;
    }

    /**
     * Returns a list of playlists.
     * @return
     */
    public ObservableList<PlayList> getPlayLists() {
        return playLists;
    }

    /**
     * Returns a queue of tracks.
     * @return
     */
    public Queue<Track> getQueue() {
        return queue;
    }

    /**
     * Adds a track to the queue.
     * @param  track
     */
    public void addToQueue(Track track) {
        queue.add(track);
    }

    /**
     * Returns the ID of the current track.

     * @return
     */
    public int getCurrentTrackID() {
        return currentTrackID;
    }

    /**
     * Sets the ID of the current track.
     *  @param currentTrackID
     */
    public void setCurrentTrackID(int currentTrackID) {
        this.currentTrackID = currentTrackID;
        this.currentTrack.set(currentPlayList.get(currentTrackID));
    }
}
