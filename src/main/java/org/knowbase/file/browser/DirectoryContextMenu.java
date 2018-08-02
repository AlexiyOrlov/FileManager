package org.knowbase.file.browser;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Stage;
import org.knowbase.Alert2;
import org.knowbase.tools.Methods;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class DirectoryContextMenu implements EventHandler<ContextMenuEvent> {

    private Path directory;

    DirectoryContextMenu(Path directory) {
        this.directory = directory;
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
                System.out.println("deleting "+directory);
            }
        });
        ContextMenu contextMenu=new ContextMenu(delete);
        contextMenu.show(stage,event.getScreenX(),event.getScreenY());
    }
}
