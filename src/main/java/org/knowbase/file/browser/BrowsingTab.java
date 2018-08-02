package org.knowbase.file.browser;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import org.knowbase.Alert2;
import org.knowbase.Dialog2;
import org.knowbase.Vbox2;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;

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
        container.setPadding(new Insets(0,6,6,6));
        scrollPane.setPadding(new Insets(6));
        tab.setContent(container);
        tab.setClosable(true);
        try {
            new PathLister(path,this).handle(null);
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
                    FileSearch fileSearch=new FileSearch(currnetDirectory,checkinsides.isSelected(),tosearch, container);
                    textSearchThread=new Thread(fileSearch);
                    textSearchThread.start();
                }
            });
            container.getChildren().addAll(goUp,search);
            FileBrowser.TAB_PANE.getTabs().add(tab);
            FileBrowser.TAB_PANE.getSelectionModel().select(tab);
            FileBrowser.browsingTabs.add(new WeakReference<>(this));
        }
        catch (Exception f)
        {
            String message=f.getMessage();
            Alert2 warn=new Alert2(Alert.AlertType.WARNING,message);
            warn.show();
        }

    }

    public Vbox2 getContainer() {
        return container;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public FlowPane getElementPane() {
        return elementPane;
    }
}
