package org.knowbase.file.browser;

import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import org.knowbase.Alert2;
import org.knowbase.Dialog2;
import org.knowbase.Vbox2;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BrowsingTab {

    Tab tab;
    private Vbox2 container;
    private FlowPane elementPane;
    private Thread textSearchThread;
    private ScrollPane scrollPane;
    Path currnetDirectory;
    public BrowsingTab(File directory) {
        this(directory.toPath());
    }

    public BrowsingTab(Path path) {
        tab=new Tab(path.getFileName()==null ? path.toString() : path.getFileName().toString());
        currnetDirectory=path;
        elementPane=new FlowPane(Orientation.VERTICAL,10,10);
        elementPane.setPrefHeight(FileBrowser.MAXIMUM_BOUNDS.getHeight()/2);
        container=new Vbox2(scrollPane=new ScrollPane(elementPane));
        tab.setContent(container);
        tab.setClosable(true);
        try {
            List<Path> paths= Files.list(path).collect(Collectors.toList());

            paths.forEach(p ->{
                if(Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
                {
                    Button button=new Button(p.getFileName().toString());
                    button.setOnAction(new PathLister(p,this));
                    button.setOnContextMenuRequested(new DirectoryContextMenu(p));
                    elementPane.getChildren().add(button);
                }
                else{
                    Text text=new Text(p.getFileName().toString());
                    elementPane.getChildren().add(text);
                }
            });
            Button goUp=new Button("Upwards");
            goUp.setOnAction(event -> {
                if(currnetDirectory.getParent()==null)
                {
                    new Alert2(Alert.AlertType.INFORMATION,path.toString()+" has no parent").show();
                }
                else{
                    currnetDirectory=currnetDirectory.getParent();
                    tab.setText(currnetDirectory.toString());
                    new PathLister(currnetDirectory,this).handle(null);
                }
            });
            Button search=new Button("Search");
            search.setOnAction(event -> {
                Vbox2 vbox2=new Vbox2();
                Dialog2<String> dialog2=new Dialog2<>("What to search?",vbox2,null,ButtonType.APPLY,ButtonType.CANCEL);
                CheckBox checkinsides=new CheckBox("Check contents");
                vbox2.getChildren().add(checkinsides);
                TextField argument=new TextField();
                vbox2.getChildren().add(argument);
                dialog2.setResultConverter(param -> argument.getText());
                Optional<String> toSearch=dialog2.showAndWait();
                if(toSearch.isPresent() && !toSearch.get().isEmpty())
                {
                    String tosearch=toSearch.get();
                    FileSearch fileSearch=new FileSearch(currnetDirectory,checkinsides.isSelected(),tosearch);
                    textSearchThread=new Thread(fileSearch);
                    textSearchThread.start();
                    Button stopthread=new Button("Stop search");
                    stopthread.setOnAction(event1 -> {
                        if(textSearchThread!=null)
                            textSearchThread.interrupt();
                        container.getChildren().remove(stopthread);
                    });
                    container.getChildren().add(stopthread);
                }
            });
            container.getChildren().addAll(goUp,search);
            FileBrowser.TAB_PANE.getTabs().add(tab);
            FileBrowser.TAB_PANE.getSelectionModel().select(tab);
        }
        catch (FileSystemException f)
        {
            String message=f.getMessage();
            Alert2 warn=new Alert2(Alert.AlertType.WARNING,message);
            warn.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Vbox2 getContainer() {
        return container;
    }

    public FlowPane getElementPane() {
        return elementPane;
    }
}
