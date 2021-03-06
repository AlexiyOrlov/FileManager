package org.knowbase.file.browser;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
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

public class PathInitializer implements EventHandler<MouseEvent> {

    private Path path;
    private BrowsingTab browsingTab;
    PathInitializer(Path path, BrowsingTab tab) {
        this.path = path;
        browsingTab=tab;
    }

    @Override
    public void handle(MouseEvent event) {
        if(event==null || event.getClickCount()==2) {
            try {
                List<Path> children = Files.list(path).collect(Collectors.toList());
                browsingTab.currentDirectory = path;
                browsingTab.tab.setText(path.toString());
                browsingTab.getElementPane().getChildren().clear();
                if (children.isEmpty()) {
                    Label label = new Label("Directory is empty");
                    label.setFont(new Font(16));
                    browsingTab.getElementPane().getChildren().add(label);
                }

                children.forEach(ch -> {
                    if (Files.isDirectory(ch)) {
                        Button button = new Button(ch.getFileName().toString());
                        button.setOnMouseClicked(new PathInitializer(ch, browsingTab));

                        button.setOnContextMenuRequested(new DirectoryContextMenu(ch, button, browsingTab));
                        browsingTab.getElementPane().getChildren().add(button);
                    } else {
                        new FMFile(ch,browsingTab);
                    }
                });
                FileBrowser.handleSorting(browsingTab);
                browsingTab.getScrollPane().setVvalue(0);

            } catch (AccessDeniedException ac) {
                Alert2 alert2 = new Alert2(Alert.AlertType.WARNING, "Access to " + ac.getMessage() + "\n is denied");
                alert2.show();
            } catch (FileSystemException f) {
                new Alert2(Alert.AlertType.WARNING, f.getMessage()).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void launchWithDefaultApplication(File file)
    {
        if (Desktop.isDesktopSupported())
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(file);
                }
                catch (Exception e) {
                    try {
                        String mime = Files.probeContentType(file.toPath());
                        if (mime != null) {
                            Platform.runLater(() -> new Alert2(Alert.AlertType.INFORMATION, "Operating system couldn't " +
                                    "determine application for - " + mime).show());
                        } else {
                            String filename = file.getName();
                            if(filename.contains(".")) {
                                String extension = filename.substring(filename.lastIndexOf('.'));
                                Platform.runLater(() -> new Alert2(Alert.AlertType.INFORMATION, "Operating system couldn't " +
                                        "determine application for " + extension).show());
                            }
                            else{
                                Platform.runLater(() -> new Alert2(Alert.AlertType.INFORMATION,"Operating " +
                                        "system can't open "+filename).show());
                            }
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }).start();
        else {
            new Alert2(Alert.AlertType.ERROR, "Desktop is unsupported on this platform").show();
        }

    }
}
