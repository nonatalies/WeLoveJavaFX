package com.player.mediaplayer.controllers;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.player.mediaplayer.PlayerContext;
import com.player.mediaplayer.models.PlayList;
import com.player.mediaplayer.models.PlayerState;
import com.player.mediaplayer.models.Track;
import com.player.mediaplayer.models.Player;
import com.player.mediaplayer.utils.MP3Parser;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class  ControlPaneController responsible for the part of the GUI that contains the controls.
 */
public class ControlPaneController implements Initializable {
    private final Player player = PlayerContext.player;
    public ImageView albumImage;
    public Text currentDuration;
    public Slider durationSlider;
    public Text totalDuration;
    public ToggleButton shuffleButton;
    public Button previousSongButton;
    public ToggleButton playSongButton;
    public Button nextSongButton;
    public ToggleButton repeatSongButton;
    public Slider volumeSlider;
    public Button folderButton;
    public Label songNameText;
    public Label authorNameText;
    public ToggleButton likedTracksButton;
    public Button showQueueButton;
    public Button showAllTracksButton;

    /**
     * Initializes UI elements.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeButtonsIcons();
        playButtonAction();
        openFolderButtonAction();
        shuffleButtonAction();
        repeatButtonAction();
        initializeVolumeSlider();
        initializeDurationSlider();
        initializeOnEndOfMediaAction();
        currentTrackChangedHandler();
        updateControlsDisable(true);
        favoriteSongsButtonAction();
        setupPlayMediaAction();
        setupPauseMediaAction();
        setShowAllTracksButtonAction();
        setQueueButtonAction();
        prevNextButtonActions();
    }

    /**
     * Updates the track cover.
     */
    private void setSongImage() {
        Image image = player.getCurrentTrack().get().getSongArtwork();
        if (image == null) {
            URL url = getClass().getResource("/com/player/mediaplayer/images/default_artwork.png");
            image = new Image(url.toString());
        }
        albumImage.setImage(image);
    }

    /**
     * Sets icons for buttons.
     */
    private void initializeButtonsIcons() {
        shuffleButton.setGraphic(new FontIcon());
        previousSongButton.setGraphic(new FontIcon());
        nextSongButton.setGraphic(new FontIcon());
        repeatSongButton.setGraphic(new FontIcon());
        folderButton.setGraphic(new FontIcon());
        playSongButton.setGraphic(new FontIcon());
        likedTracksButton.setGraphic(new FontIcon());
        showQueueButton.setGraphic(new FontIcon());
        showAllTracksButton.setGraphic(new FontIcon());
    }

    /**
     * The logic of the buttons to go to the previous / next track.
     */
    private void prevNextButtonActions() {
        previousSongButton.setOnAction(actionEvent -> {
            playPreviousSong();
        });
        nextSongButton.setOnAction(actionEvent -> {
            playNextSong();
        });
    }

    /**
     * The logic of displaying a list of all tracks.
     */
    private void setShowAllTracksButtonAction() {
        showAllTracksButton.setOnMouseClicked(mouseEvent -> {
            PlayerContext.selectedPlaylist.clear();
            PlayerContext.selectedPlaylist.addAll(player.getAllTracks());
            PlayerContext.selectedPlaylistRef = player.getAllTracks();
            PlayerContext.selectedPlaylistName.setValue("All tracks");
        });
    }

    /**
     * The logic of adding a track to the queue.
     */
    private void setQueueButtonAction() {
        showQueueButton.setOnMouseClicked(mouseEvent -> {
            PlayerContext.selectedPlaylist.clear();
            PlayerContext.selectedPlaylist.addAll(player.getQueue());
            PlayerContext.selectedPlaylistName.setValue("Queue");
        });
    }

    /**
     * Changes the disabled property for all elements of this ControlPane.
     */
    private void updateControlsDisable(Boolean disabled) {
        playSongButton.setDisable(disabled);
        previousSongButton.setDisable(disabled);
        nextSongButton.setDisable(disabled);
        shuffleButton.setDisable(disabled);
        repeatSongButton.setDisable(disabled);
        durationSlider.setDisable(disabled);
        volumeSlider.setDisable(disabled);
    }

    /**
     * Sets the action to be performed at the moment when the track playback ends - the transition to the next track will be performed.
     */
    private void initializeOnEndOfMediaAction() {
        player.setOnEndOfMedia(() -> playNextSong());
    }

    /**
     * Switching the random playback mode.
     */
    private void shuffleButtonAction() {
        shuffleButton.setOnAction(actionEvent -> player.setIsShuffling(shuffleButton.isSelected()));
    }

    /**
     * Switching the playback mode to repeat.
     */
    private void repeatButtonAction() {
        repeatSongButton.setOnAction(actionEvent -> player.setIsRepeating(repeatSongButton.isSelected()));
    }

    /**
     * Sets the action when the play/pause button is pressed.
     */
    private void playButtonAction() {
        playSongButton.setOnMouseClicked(mouseEvent -> {
            if (playSongButton.isSelected()) {
                resumeMedia();
            } else {
                pauseMedia();
            }
        });
    }

    /**
     * Opens a dialog box that allows you to specify the path to the folder with tracks.
     * All tracks in the selected folder will be added to the playlist.
     */
    private void openFolderButtonAction() {
        folderButton.setOnMousePressed(mouseEvent -> {
            DirectoryChooser directoryChoose = new DirectoryChooser();
            File directory = directoryChoose.showDialog(new Stage());
            try {
                if (directory != null) {
                    File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
                    if (files != null) {
                        for (File file : files) {
                            player.addTrack(MP3Parser.parse(file));
                        }
                    }
                    player.setCurrentPlayList(player.getAllTracks());
                }
            } catch (InvalidDataException | UnsupportedTagException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Configures the handler of the "track changed" event.
     * Updates the displayed information about the current track.
     * Starts playing a new track.
     */
    private void currentTrackChangedHandler() {
        player.getCurrentTrack().addListener(new ChangeListener<Track>() {
            @Override
            public void changed(ObservableValue<? extends Track> observableValue, Track track, Track t1) {
                updateControlsDisable(false);
                updateTrackInfo();
                player.play();
            }
        });
    }

    /**
     * Updates information about the current track.
     */
    private void updateTrackInfo() {
        Track trackToPlay = player.getCurrentTrack().get();
        songNameText.setText(trackToPlay.getSongName());
        authorNameText.setText(trackToPlay.getSongArtist());
        totalDuration.setText(trackToPlay.getSongDuration());
        setSongImage();
    }

    /**
     * Sets the actions to be performed at the start of the track playback.
     * Changes the icon of the play/pause button, updates the track information. Restarts the timer.
     */
    private void setupPlayMediaAction() {
        player.setOnPlay(() -> {
            if (PlayerContext.globalTimer != null) {
                stopTimer();
            }
            startTimer();
            if (!playSongButton.isSelected()) {
                playSongButton.fire();
            }
            updateTrackInfo();
        });
    }

    /**
     * Resumes playback.
     */
    private void resumeMedia() {
        player.resume();
        startTimer();
    }

    /**
     * Specifies the actions to be performed when the track is paused.
     * Stops the timer.
     */
    private void setupPauseMediaAction() {
        player.setOnPause(() -> stopTimer());
    }

    /**
     * Pauses playback.
     */
    private void pauseMedia() {
        player.pause();
    }

    /**
     * Calls the previous method of the player, stops the timer.
     */
    private void playPreviousSong() {
        if (PlayerContext.globalTimer != null) {
            stopTimer();
        }
        if (!player.previous()) {
            player.play();
        }
    }

    /**
     * Calls the player's next method, stops the timer.
     */
    private void playNextSong() {
        if (PlayerContext.globalTimer != null) {
            stopTimer();
        }
        player.next();
    }

    /**
     * Starts the timer.
     */
    private void startTimer() {
        if (PlayerContext.globalTimer != null) {
            throw new IllegalStateException("Timer not stopped");
        }
        PlayerContext.globalTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                double currentTime = player.getMediaPlayer().getCurrentTime().toSeconds();
                double totalDuration = player.getMediaPlayer().getTotalDuration().toSeconds();
                if (!durationSlider.isValueChanging()) {
                    double newSliderValue = currentTime / totalDuration;
                    if (Double.isNaN(newSliderValue)) {
                        newSliderValue = 0;
                    }
                    durationSlider.setValue(newSliderValue);
                }
            }
        };
        PlayerContext.globalTimer.schedule(timerTask, 0, 1000);
    }

    /**
     * Stops the timer.
     */
    private void stopTimer() {
        PlayerContext.globalTimer.cancel();
        PlayerContext.globalTimer = null;
    }

    /**
     * Logic of changing the value of the volume slider.
     */
    private void initializeVolumeSlider() {
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        PlayerState state = player.getLoadedState();
        volumeSlider.setValue(state != null ? state.currentVolume : 0.5);
        player.getCurrentVolume().bind(volumeSlider.valueProperty());
        volumeSlider.styleProperty().bind(Bindings.concat("-fx-gradient-color: linear-gradient(to right, -fx-track-color ")
                .concat(volumeSlider.valueProperty().multiply(100))
                .concat("%, #a9a9a9 ")
                .concat(volumeSlider.valueProperty().multiply(100))
                .concat("%) !important;"));
        player.getCurrentVolume().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                player.getMediaPlayer().setVolume(player.getCurrentVolume().get());
            }
        });
    }

    /**
     * Logic of rendering hover animations.
     */
    private void sliderHoverActions() {
        durationSlider.hoverProperty().addListener((event) -> {
            StackPane thumb = (StackPane) durationSlider.lookup(".thumb");
            if (durationSlider.isHover()) {
                durationSlider.getStyleClass().add("slider-active-color");
                thumb.getStyleClass().add("thumb-active-color");
            } else {
                durationSlider.getStyleClass().remove("slider-active-color");
                thumb.getStyleClass().remove("thumb-active-color");
            }
        });
        volumeSlider.hoverProperty().addListener((event) -> {
            StackPane thumb = (StackPane) volumeSlider.lookup(".thumb");
            if (volumeSlider.isHover()) {
                volumeSlider.getStyleClass().add("slider-active-color");
                thumb.getStyleClass().add("thumb-active-color");
            } else {
                volumeSlider.getStyleClass().remove("slider-active-color");
                thumb.getStyleClass().remove("thumb-active-color");
            }
        });
        durationSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            durationSlider.getStyleClass().add("slider-active-color");
            (durationSlider.lookup(".thumb")).getStyleClass().add("thumb-active-color");
        });
        durationSlider.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            durationSlider.getStyleClass().remove("slider-active-color");
            (durationSlider.lookup(".thumb")).getStyleClass().remove("thumb-active-color");
        });
        volumeSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            volumeSlider.getStyleClass().add("slider-active-color");
            (volumeSlider.lookup(".thumb")).getStyleClass().add("thumb-active-color");
        });
        volumeSlider.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            volumeSlider.getStyleClass().remove("slider-active-color");
            (volumeSlider.lookup(".thumb")).getStyleClass().remove("thumb-active-color");
        });
    }

    /**
     * Logic of changing the value of the duration slider.
     */
    private void initializeDurationSlider() {
        durationSlider.setMin(0);
        durationSlider.setMax(1);
        durationSlider.setValue(0);
        durationSlider.valueProperty().addListener((observableValue, number, t1) -> {
            double newCurrentDuration = observableValue.getValue().doubleValue();
            currentDuration.setText(MP3Parser.parseSongLength((int) player.getMediaPlayer().getTotalDuration().multiply(newCurrentDuration).toSeconds()));
            String style = String.format("-fx-background-color: linear-gradient(to right, -fx-track-color %d%%, #a9a9a9 %d%%) !important;",
                    (int) (newCurrentDuration * 100), (int) (newCurrentDuration * 100));
            durationSlider.lookup(".track").setStyle(style);
        });
        durationSlider.setOnMouseReleased((MouseEvent event) -> player.getMediaPlayer().seek(player.getMediaPlayer().getTotalDuration().multiply(durationSlider.getValue())));
        durationSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> durationSlider.setValueChanging(true));
        durationSlider.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> durationSlider.setValueChanging(false));
        sliderHoverActions();
    }

    /**
     * Hiding/displaying "favorite tracks".
     */
    private void favoriteSongsButtonAction() {
        likedTracksButton.setOnMouseClicked(mouseEvent -> player.setOnlyFavorites(likedTracksButton.isSelected()));
    }
}
