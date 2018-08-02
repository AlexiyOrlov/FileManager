package org.knowbase.file.browser;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.knowbase.Alert2;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class PathLister implements EventHandler<ActionEvent> {

    private Path path;
    private BrowsingTab browsingTab;
    PathLister(Path path,BrowsingTab tab) {
        this.path = path;
        browsingTab=tab;
    }

    @Override
    public void handle(ActionEvent event) {
        try {
            List<Path> children= Files.list(path).collect(Collectors.toList());
                browsingTab.currnetDirectory=path;
                browsingTab.tab.setText(path.toString());
                    browsingTab.getElementPane().getChildren().clear();
                if(children.isEmpty())
                {
                    Label label=new Label("Directory is empty");
                    label.setFont(new Font(16));
                    browsingTab.getElementPane().getChildren().add(label);
                }
                else{
                    children.sort(new FileSorter());
                }
                children.forEach(ch -> {
                    if (Files.isDirectory(ch)) {
                        Button button = new Button(ch.getFileName().toString());
                        button.setOnAction(new PathLister(ch, browsingTab));
                        button.setOnContextMenuRequested(new DirectoryContextMenu(ch));
                        browsingTab.getElementPane().getChildren().add(button);
                    }
                    else{
                        Text text=new Text(ch.getFileName().toString());
                        browsingTab.getElementPane().getChildren().add(text);
                        text.setOnMouseClicked(mouseEvent -> {
                            if(mouseEvent.getClickCount()==2)
                            {
                                File file=new File(browsingTab.currnetDirectory.toFile(),text.getText());
                                try {
                                    if(Desktop.isDesktopSupported())
                                        new Thread(() -> {
                                            try {
                                                Desktop.getDesktop().open(file);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }).start();
                                    else{
                                        new Alert2(Alert.AlertType.ERROR,"Desktop is unsupported on this platform").show();
                                    }
                                } catch (Exception e) {
                                    try {
                                        String filename=text.getText();
                                        String mime=Files.probeContentType(file.toPath());
                                        if(mime!=null)
                                        {
                                            new Alert2(Alert.AlertType.INFORMATION,"Operating system couldn't " +
                                                    "determine application for - "+mime).show();
                                        }
                                        else{
                                            String extension=filename.substring(filename.lastIndexOf('.'));
                                            new Alert2(Alert.AlertType.INFORMATION,"Operating system couldn't " +
                                                    "determine application for - "+extension).show();
                                        }
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                });

        }
        catch (AccessDeniedException ac)
        {
            Alert2 alert2=new Alert2(Alert.AlertType.WARNING,"Access to "+ac.getMessage()+"\n is denied");
            alert2.show();
        }
        catch (FileSystemException f)
        {
            new Alert2(Alert.AlertType.WARNING,f.getMessage()).show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
