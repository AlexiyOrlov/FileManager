package org.knowbase.file.browser;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.knowbase.Alert2;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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
                    }
                });

        }
        catch (AccessDeniedException ac)
        {
            Alert2 alert2=new Alert2(Alert.AlertType.WARNING,"Access to "+ac.getMessage()+"\n is denied");
            alert2.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
