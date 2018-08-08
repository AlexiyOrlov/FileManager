package org.knowbase.file.browser;

import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.FlowPane;
import org.knowbase.Alert2;
import org.knowbase.tools.Methods;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DirectoryContextMenu implements EventHandler<ContextMenuEvent> {

    private Path directory;
    private Button associatedButton;
    private FlowPane flowPane;
    private BrowsingTab browsingTab;

    DirectoryContextMenu(Path directory, Button button, BrowsingTab tab) {
        this.directory = directory;
        associatedButton=button;
        flowPane=tab.getElementPane();
        browsingTab=tab;
    }

    @Override
    public void handle(ContextMenuEvent event) {

        MenuItem delete=new MenuItem("Delete");
        delete.setOnAction(e -> {
            Alert2 alert2=new Alert2(Alert.AlertType.CONFIRMATION,"Do you want to permanently delete "+
                    directory.toString() + "?");
            Optional<ButtonType> optionalButtonType=alert2.showAndWait();
            if(optionalButtonType.isPresent() && optionalButtonType.get()==ButtonType.OK)
            {

                boolean success=Methods.delete(directory);
                if(success)
                {
                    browsingTab.getElementPane().getChildren().remove(associatedButton);
                    browsingTab.getScrollPane().requestLayout();
                }
                else{
//                    StringBuilder stringBuilder=new StringBuilder("Couldn't delete files:\n");
//                    success.forEach(path -> stringBuilder.append(path.toString()).append('\n'));
                    Alert2 alert=new Alert2(Alert.AlertType.WARNING,"Couldn't delete "+directory.toString());
                    alert.show();
                }
            }
        });
        MenuItem info=new MenuItem("Show info");
        info.setOnAction(new FileInformator(directory));
        ContextMenu contextMenu=new ContextMenu(info,delete);
        contextMenu.show(FileBrowser.stage,event.getScreenX(),event.getScreenY());
    }
}
