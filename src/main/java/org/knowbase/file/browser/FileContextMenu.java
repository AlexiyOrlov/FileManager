package org.knowbase.file.browser;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.knowbase.Alert2;
import org.knowbase.tools.Methods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
                boolean success=Methods.delete(file);
                if(success)
                {
                    tab.getElementPane().getChildren().remove(associatedControl);
                    tab.getScrollPane().requestLayout();
                }
                else{

                    Alert2 alert2=new Alert2(Alert.AlertType.WARNING,"No access to this file");
                    alert2.show();
                }
            }
        });
        MenuItem rename=new MenuItem("Rename");
        rename.setOnAction(event1 -> {
            TextInputDialog textInputDialog=new TextInputDialog(file.getFileName().toString());
            textInputDialog.setHeaderText("Rename file");
            Optional<String> stringOptional=textInputDialog.showAndWait();
            if(stringOptional.isPresent() && !stringOptional.get().isEmpty())
            {
                String string=stringOptional.get();
                try {
                    file=Files.copy(file, file.resolveSibling(string));
                    if(associatedControl instanceof FMFile)
                        ((FMFile) associatedControl).setText(string);
                    else if(associatedControl instanceof Button)
                        ((Button) associatedControl).setText(string);
                } catch (IOException e) {
                    new Alert2(Alert.AlertType.ERROR,e.getMessage()).show();
                }
            }
        });
        MenuItem copy=new MenuItem("Copy");
        copy.setOnAction(event1 -> {
            DirectoryChooser directoryChooser=new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(FileBrowser.SETTINGS.getOrDefault(FileBrowser.LAST_DIRECTORY,file.getParent().toString())));
            directoryChooser.setTitle("Copy to");
            File dir=directoryChooser.showDialog(FileBrowser.stage);
            if(dir!=null)
            {
                FileBrowser.SETTINGS.put(FileBrowser.LAST_DIRECTORY,dir.getAbsolutePath());
                try {
                    Files.copy(file,dir.toPath().resolve(file.getFileName()));
                } catch (IOException e) {
                    new Alert2(Alert.AlertType.ERROR,e.getMessage()).show();
                }
            }
        });
        MenuItem info=new MenuItem("Show info");
        info.setOnAction(new FileInformator(file));

        ContextMenu contextMenu=new ContextMenu(info,rename,copy,delete);
        contextMenu.show(FileBrowser.stage,event.getScreenX(),event.getScreenY());
    }
}
