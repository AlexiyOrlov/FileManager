package org.knowbase.file.browser;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import org.knowbase.Dialog2;
import org.knowbase.Vbox2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.stream.Stream;

/**
 * Created on 8/3/18 by alexiy.
 */
public class FileInformator implements EventHandler<ActionEvent> {
    private Path file;

    public FileInformator(Path file) {
        this.file = file;
    }

    @Override
    public void handle(ActionEvent event) {
        Dialog2<Object, Vbox2> dialog2=new Dialog2<>(file.getFileName()+" info",new Vbox2(), Modality.NONE, ButtonType.CLOSE);
        ObservableList<Node> observableList=dialog2.getContainer().getChildren();
        if(Files.isReadable(file))
        {
            if(!Files.isDirectory(file)) {
                try {
                    long size = Files.size(file);
                    Label label;
                    if (size >= Math.pow(1024, 3)) {
                        label = new Label("Size: " + (size / Math.pow(1024, 3)) + " GiB");
                    } else if (size >= 1024 * 1024) {
                        label = new Label("Size: " + (size / (1024 * 1024)) + " MiB");
                    } else if (size >= 1024) {
                        label = new Label("Size: " + size / 1024 + " KiB");
                    } else label = new Label("Size: " + size + " bytes");
                    observableList.add(label);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                try {
                    long folders=Files.list(file).filter(path -> Files.isDirectory(path,LinkOption.NOFOLLOW_LINKS)).count();
                    long files=Files.list(file).filter(path -> !Files.isDirectory(path,LinkOption.NOFOLLOW_LINKS)).count();
                    if(folders>0)
                    {
                        observableList.add(new Label("Immediate folders: "+folders));
                    }
                    if(files>0)
                        observableList.add(new Label("Immediate files: "+files));
                    if(folders==0 && files==0)
                        observableList.add(new Label("Empty"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            if(!Files.isDirectory(file) && Files.isExecutable(file))
                observableList.add(new Label("Executable"));
        }
        else{
            observableList.add(new Label("No permission to read this file"));
        }
        dialog2.show();
    }
}
