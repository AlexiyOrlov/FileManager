package org.knowbase.file.browser;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.knowbase.Vbox2;
import org.knowbase.tools.Settings;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

public class FileBrowser extends Application {

    static Rectangle2D MAXIMUM_BOUNDS;
    static Stage stage;
    static Settings SETTINGS;
    static TabPane TAB_PANE;
    static FileSort fileSort;
    static ArrayList<WeakReference<BrowsingTab>> browsingTabs;
    private final static String SETTING_FILE="settings.txt", LAST_DIRECTORY="last_directory", FILE_SORTING="file_sorting_type";
    @Override
    public void start(Stage primaryStage)   {
        MAXIMUM_BOUNDS= Screen.getPrimary().getVisualBounds();
        stage=primaryStage;
        SETTINGS=new Settings(Paths.get(SETTING_FILE));
        fileSort=FileSort.valueOf(SETTINGS.getOrDefault(FILE_SORTING,FileSort.ALPHABETIC.name()));
        browsingTabs=new ArrayList<>();
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
        ToggleGroup toggleGroup=new ToggleGroup();
        Menu operations=new Menu("Operations",null,createTab);
        RadioMenuItem extensionSort=new RadioMenuItem("By file extension");
        extensionSort.setToggleGroup(toggleGroup);
        extensionSort.setOnAction(event -> {
            fileSort=FileSort.EXTENSION;
            browsingTabs.forEach(this::handleSorting);
        });
        RadioMenuItem sortDefault=new RadioMenuItem("By name");
        sortDefault.setToggleGroup(toggleGroup);
        sortDefault.setOnAction(event -> {
            fileSort=FileSort.ALPHABETIC;
            browsingTabs.forEach(this::handleSorting);
        });
        if(fileSort==FileSort.EXTENSION)
            toggleGroup.selectToggle(extensionSort);
        else if(fileSort==FileSort.ALPHABETIC)
            toggleGroup.selectToggle(sortDefault);
        Menu sorting=new Menu("Sorting",null,sortDefault,extensionSort);
        MenuBar menuBar=new MenuBar(operations,sorting);
        Vbox2 mainContainer=new Vbox2(menuBar,TAB_PANE);
        roots.forEach(BrowsingTab::new);
        Scene scene=new Scene(mainContainer,MAXIMUM_BOUNDS.getWidth()-300,MAXIMUM_BOUNDS.getHeight()-300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("File Manager");
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    static void handleSorting(BrowsingTab browsingTab)
    {
        SETTINGS.put(FILE_SORTING,fileSort.name());
        if(browsingTab!=null) {
            if (fileSort == FileSort.ALPHABETIC) {


                ArrayList<Node> buffer = new ArrayList<>(browsingTab.getElementPane().getChildren());
                browsingTab.getElementPane().getChildren().clear();
                buffer.sort((o1, o2) -> {
                    if (o1 instanceof Button && !(o2 instanceof Button))
                        return -1;
                    if (!(o1 instanceof Button) && o2 instanceof Button)
                        return 1;
                    if (o1 instanceof Text && o2 instanceof Text)
                        return ((Text) o1).getText().compareTo(((Text) o2).getText());
                    if (o1 instanceof Button)
                        return ((Button) o1).getText().compareTo(((Button) o2).getText());
                    return 0;
                });
                browsingTab.getElementPane().getChildren().addAll(buffer);


            } else if (fileSort == FileSort.EXTENSION) {

                ArrayList<Node> nodes = new ArrayList<>(browsingTab.getElementPane().getChildren());
                browsingTab.getElementPane().getChildren().clear();

                nodes.sort((o1, o2) -> {
                    if (o1 instanceof Button && !(o2 instanceof Button))
                        return -1;
                    if (!(o1 instanceof Button) && o2 instanceof Button)
                        return 1;
                    if (o1 instanceof Text && o2 instanceof Text) {
                        String string = ((Text) o1).getText();
                        String s2 = ((Text) o2).getText();
                        if (string.contains(".") && s2.contains(".")) {
                            String ext = string.substring(string.lastIndexOf('.') + 1);
                            String e2 = s2.substring(s2.lastIndexOf('.') + 1);
                            if(ext.equals(e2))
                                return string.compareTo(s2);
                            return ext.compareTo(e2);
                        }
                        return string.compareTo(s2);
                    }
                    if(o1 instanceof Button)
                        return ((Button) o1).getText().compareTo(((Button) o2).getText());
                    return 0;
                });
                browsingTab.getElementPane().getChildren().addAll(nodes);


            }
        }
    }

    void handleSorting(WeakReference<BrowsingTab> tab)
    {
        BrowsingTab browsingTab = tab.get();
        handleSorting(browsingTab);
    }

    @Override
    public void stop()   {
        SETTINGS.save();
    }
}
