package org.knowbase.file.browser;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.knowbase.Vbox2;
import org.knowbase.tools.Settings;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileBrowser extends Application {

    static Rectangle2D MAXIMUM_BOUNDS;
    static Stage stage;
    static Settings SETTINGS;
    static TabPane TAB_PANE;
    private final static String SETTING_FILE="settings.txt", LAST_DIRECTORY="last_directory";
    @Override
    public void start(Stage primaryStage)   {
        MAXIMUM_BOUNDS= Screen.getPrimary().getVisualBounds();
        stage=primaryStage;
        SETTINGS=new Settings(Paths.get(SETTING_FILE));
        FileSystem defFileSystem=FileSystems.getDefault();
        Iterable<Path> roots=defFileSystem.getRootDirectories();
        TAB_PANE=new TabPane();
        MenuItem createTab=new MenuItem("New tab");
        createTab.setOnAction(event -> {
            DirectoryChooser directoryChooser=new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(SETTINGS.getOrDefault(LAST_DIRECTORY,System.getProperty("user.home"))));
            File dir=directoryChooser.showDialog(primaryStage);
            if(dir!=null)
            {
                new BrowsingTab(dir);
                SETTINGS.put(LAST_DIRECTORY,dir.getAbsolutePath());
            }
        });
        Menu operations=new Menu("Operations",null,createTab);
        MenuBar menuBar=new MenuBar(operations);
        Vbox2 mainContainer=new Vbox2(menuBar,TAB_PANE);
        roots.forEach(BrowsingTab::new);
        Scene scene=new Scene(mainContainer,MAXIMUM_BOUNDS.getWidth()-300,MAXIMUM_BOUNDS.getHeight()-300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("File Manager");
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    @Override
    public void stop()   {
        SETTINGS.save();
    }
}
