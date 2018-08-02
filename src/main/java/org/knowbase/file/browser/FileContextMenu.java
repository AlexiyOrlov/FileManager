package org.knowbase.file.browser;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import org.knowbase.Alert2;
import org.knowbase.tools.Methods;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Created on 8/2/18 by alexiy.
 */
public class FileContextMenu implements EventHandler<ContextMenuEvent> {

    private Node associatedControl;
    private Path file;
    private BrowsingTab tab;

    public FileContextMenu(Path file, Node associatedControl, BrowsingTab browsingTab) {
        this.associatedControl = associatedControl;
        this.file = file;
        this.tab = browsingTab;
    }

    @Override
    public void handle(ContextMenuEvent event) {
        MenuItem delete=new MenuItem("Delete");
        delete.setOnAction(event1 -> {
            Alert2 confirm=new Alert2(Alert.AlertType.CONFIRMATION,"Do you want to permanently delete "+file+"?");
            Optional<ButtonType> buttonType=confirm.showAndWait();
            if(buttonType.isPresent() && buttonType.get()==ButtonType.OK)
            {
                Methods.delete(file);
                tab.getElementPane().getChildren().remove(associatedControl);
                tab.getScrollPane().requestLayout();
            }
        });

        ContextMenu contextMenu=new ContextMenu(delete);
        contextMenu.show(FileBrowser.stage,event.getScreenX(),event.getScreenY());
    }
}
