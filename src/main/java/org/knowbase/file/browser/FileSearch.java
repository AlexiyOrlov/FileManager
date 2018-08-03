package org.knowbase.file.browser;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
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

    public FileSearch(Path in, boolean scanInside, String argument, Pane container) {
        this.in = in;
        this.scanInside = scanInside;
        this.argument = argument;
        progressBar=new ProgressBar(0);
        setOnScheduled(event -> {
            stopButton=new Button("Abort search");
            stopButton.setOnAction(event1 -> {
                container.getChildren().remove(stopButton);
                container.getChildren().remove(progressBar);
                cancel();
            });
            progressBar.progressProperty().bind(progressProperty());
            progressBar.setPrefWidth(FileBrowser.MAXIMUM_BOUNDS.getWidth()/2);
            container.getChildren().add(progressBar);
            container.getChildren().add(stopButton);

        });

        setOnSucceeded(event -> {
            List<Path> pathList= (List<Path>) event.getSource().getValue();
            pathList.sort(null);
            Tab tab=new Tab("Search results ("+in.toString()+"):");
            ListView<Path> listView=new ListView<>(FXCollections.observableArrayList(pathList));
            tab.setContent(listView);
            FileBrowser.TAB_PANE.getTabs().add(tab);

            container.getChildren().remove(progressBar);
            container.getChildren().remove(stopButton);
        });
        setOnFailed(event -> {
            container.getChildren().remove(progressBar);
            container.getChildren().remove(stopButton);
        });

    }

    @Override
    protected List<Path> call() {
        List<Path> applicable=new ArrayList<>();
        List<Path> pathList= Methods.getFiles(in,new ArrayList<>());
        int fileMaount=pathList.size();
        int progress=0;
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
}
