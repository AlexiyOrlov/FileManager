package org.knowbase.file.browser;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.knowbase.Alert2;
import org.knowbase.tools.Methods;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class DirectoryContextMenu implements EventHandler<ContextMenuEvent> {

    private Path directory;
    private Button associatedButton;
    private FlowPane flowPane;

    DirectoryContextMenu(Path directory, Button button, FlowPane elementPane) {
        this.directory = directory;
        associatedButton=button;
        flowPane=elementPane;
    }

    @Override
    public void handle(ContextMenuEvent event) {
        Stage stage=FileBrowser.stage;
        MenuItem delete=new MenuItem("Delete");
        delete.setOnAction(e -> {
            Alert2 alert2=new Alert2(Alert.AlertType.CONFIRMATION,"Do you want to permanently delete "+
                    directory.toString() + "?");
            Optional<ButtonType> optionalButtonType=alert2.showAndWait();
            if(optionalButtonType.isPresent() && optionalButtonType.get()==ButtonType.OK)
            {
                Methods.delete(directory);
                flowPane.getChildren().remove(associatedButton);

            }
        });
        ContextMenu contextMenu=new ContextMenu(delete);
        contextMenu.show(stage,event.getScreenX(),event.getScreenY());
    }
}
