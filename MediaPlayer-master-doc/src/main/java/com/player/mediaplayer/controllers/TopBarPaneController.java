package com.player.mediaplayer.controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Class TopBarPaneController responsible for displaying the custom TopBar.
 */
public class TopBarPaneController implements Initializable {
    public Button minimizeButton;
    public ToggleButton maximizeButton;
    public Button closeButton;

    /**
     * Initializes UI elements.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeButtonsIcons();
        initializeButtonActions();
        initializeHoverActions();
    }

    /**
     * Sets icons for buttons.
     */
    private void initializeButtonsIcons() {
        minimizeButton.setGraphic(new FontIcon());
        maximizeButton.setGraphic(new FontIcon());
        closeButton.setGraphic(new FontIcon());
    }

    /**
     * Sets the logic when clicking the collapse, expand, close buttons.
     */
    private void initializeButtonActions() {
        minimizeButton.setOnMouseClicked(mouseEvent -> {
            ((Stage)((Node) mouseEvent.getSource()).getScene().getWindow()).setIconified(true);
        });
        maximizeButton.setOnMouseClicked(mouseEvent -> {
            if (maximizeButton.isSelected()) {
                ((Stage)((Node) mouseEvent.getSource()).getScene().getWindow()).setMaximized(true);
            } else {
                ((Stage)((Node) mouseEvent.getSource()).getScene().getWindow()).setMaximized(false);
            }
        });
        closeButton.setOnMouseClicked(mouseEvent -> {
            Platform.exit();
        });
    }

    /**
     * Effects of hovering the cursor over buttons.
     */
    private void initializeHoverActions() {
        minimizeButton.hoverProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (minimizeButton.isHover()) {
                    minimizeButton.getStyleClass().add("top-bar-button-active-color");
                } else {
                    minimizeButton.getStyleClass().remove("top-bar-button-active-color");
                }
            }
        });
        maximizeButton.hoverProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (maximizeButton.isHover()) {
                    maximizeButton.getStyleClass().add("top-bar-button-active-color");
                } else {
                    maximizeButton.getStyleClass().remove("top-bar-button-active-color");
                }
            }
        });
        closeButton.hoverProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (closeButton.isHover()) {
                    closeButton.getStyleClass().add("close-button-active-color");
                } else {
                    closeButton.getStyleClass().remove("close-button-active-color");
                }
            }
        });
    }
}
