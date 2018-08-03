package org.knowbase.file.browser;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.knowbase.Hbox2;
import org.knowbase.Vbox2;
import org.knowbase.tools.Methods;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class FileSearch extends Task<List<Path>> {

    private Path in;
    private boolean scanInside;
    private String argument;
    private Button stopButton;
    private ProgressBar progressBar;
    private Label stateMessage;

    public FileSearch(Path in, boolean scanInside, String argument, Pane container) {
        this.in = in;
        this.scanInside = scanInside;
        this.argument = argument;
        progressBar=new ProgressBar(0);
        stateMessage=new Label();
        stateMessage.textProperty().bind(messageProperty());
        setOnScheduled(event -> {
            stopButton=new Button("Abort search");
            stopButton.setOnAction(event1 -> {
                removeNodes(container);
                cancel();
            });
            progressBar.progressProperty().bind(progressProperty());
            progressBar.setPrefWidth(FileBrowser.MAXIMUM_BOUNDS.getWidth()/2);
            addNodes(container);

        });

        setOnSucceeded(event -> {
            List<Path> pathList= (List<Path>) event.getSource().getValue();
            pathList.sort(null);
            Tab tab=new Tab("Search results ("+in.toString()+"):");
            ListView<Path> listView=new ListView<>(FXCollections.observableArrayList(pathList));
            listView.setOnMouseClicked(event1 -> {
                if(event1.getClickCount()==2)
                    PathInitializer.launchWithDefaultApplication(listView.getSelectionModel().getSelectedItem().toFile());
            });
            listView.setOnContextMenuRequested(event1 -> {
                MenuItem openParent=new MenuItem("Open location");
                openParent.setOnAction(event2 -> new BrowsingTab(listView.getSelectionModel().getSelectedItem().getParent()));
                ContextMenu contextMenu=new ContextMenu(openParent);
                contextMenu.show(FileBrowser.stage,event1.getScreenX(),event1.getScreenY());
            });
            tab.setContent(listView);
            FileBrowser.TAB_PANE.getTabs().add(tab);

            removeNodes(container);
        });
        setOnFailed(event -> removeNodes(container));

    }

    @Override
    protected List<Path> call() {
        List<Path> applicable=new ArrayList<>();
        updateMessage("Parsing files");
        List<Path> pathList= Methods.getFiles(in,new ArrayList<>());
        int fileMaount=pathList.size();
        int progress=0;
        updateMessage("Searching");
        for (Path path : pathList) {
            try {
                if (scanInside) {
                    List<String> strings = Files.readAllLines(path, Charset.defaultCharset());
                    for (String string : strings) {
                        if (string.contains(argument)) {
                            applicable.add(path);
                            break;
                        }
                    }
                } else {
                    String filename = path.getFileName().toString();
                    if (filename.contains(argument))
                        applicable.add(path);
                }
            } catch (IOException ignored) {
            }
            progress++;
            updateProgress(progress,fileMaount);
            if(isCancelled())
                break;
        }
        return applicable;
    }

    private void addNodes(Pane pane)
    {
        pane.getChildren().add(stateMessage);
        pane.getChildren().add(progressBar);
        pane.getChildren().add(stopButton);
    }

    private void removeNodes(Pane pane)
    {

        pane.getChildren().remove(progressBar);
        pane.getChildren().remove(stopButton);
        pane.getChildren().remove(stateMessage);
    }

}
