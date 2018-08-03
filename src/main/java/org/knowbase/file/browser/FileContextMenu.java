package org.knowbase.file.browser;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import org.knowbase.Alert2;
import org.knowbase.Dialog2;
import org.knowbase.Vbox2;
import org.knowbase.tools.Methods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
                    if(associatedControl instanceof Text)
                        ((Text) associatedControl).setText(string);
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
                try {
                    Files.copy(file,dir.toPath().resolve(file.getFileName()));
                } catch (IOException e) {
                    new Alert2(Alert.AlertType.ERROR,e.getMessage()).show();
                }
            }
        });
        MenuItem info=new MenuItem("Show info");
        info.setOnAction(event1 -> {
            Dialog2<Object,Vbox2> dialog2=new Dialog2<>(file.getFileName()+" info",new Vbox2(), Modality.NONE,ButtonType.CLOSE);
            ObservableList<Node> observableList=dialog2.getContainer().getChildren();
            if(Files.isReadable(file))
            {
                try {
                    long size=Files.size(file);
                    Label label;
                    if(size>=Math.pow(1024,3))
                    {
                        label=new Label("Size: "+(size/Math.pow(1024,3))+" GiB");
                    }
                    else if(size>=1024*1024)
                    {
                        label=new Label("Size: "+(size/(1024*1024))+ " MiB");
                    }
                    else if(size>=1024)
                    {
                        label=new Label("Size: "+size/1024+" KiB");
                    }
                    else label=new Label("Size: "+size+" bytes");
                    observableList.add(label);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(Files.isWritable(file))
                {
                    observableList.add(new Label("Writeable"));
                }
                try {
                    FileTime lastModTime=Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS);
                    observableList.add(new Label("Modified: "+lastModTime.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if(Files.isHidden(file))
                        observableList.add(new Label("Hidden"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(Files.isExecutable(file))
                    observableList.add(new Label("Executable"));
            }
            else{
                observableList.add(new Label("No permission to read this file"));
            }
            dialog2.show();
        });

        ContextMenu contextMenu=new ContextMenu(info,rename,copy,delete);
        contextMenu.show(FileBrowser.stage,event.getScreenX(),event.getScreenY());
    }
}
