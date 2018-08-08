package org.knowbase.file.browser;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.File;
import java.nio.file.Path;

public class FMFile extends StackPane{
    private Path file;
    private Text name;
    private BrowsingTab owner;
    private Rectangle background;

    public FMFile( Path file, BrowsingTab owner) {
        this.file = file;
        this.name = new Text(file.getFileName().toString());
        this.owner = owner;
        Rectangle rectangle=new Rectangle(name.getLayoutBounds().getWidth(),name.getLayoutBounds().getHeight());
        rectangle.setFill(null);
        background=rectangle;
        getChildren().addAll(rectangle,name);
        setAlignment(Pos.CENTER_LEFT);
        name.setOnContextMenuRequested(new FileContextMenu(file, this, owner));
        name.setOnMouseEntered(event1 -> name.setUnderline(true));
        name.setOnMouseExited(event1 -> name.setUnderline(false));
        name.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                File f = new File(owner.currentDirectory.toFile(), name.getText());
                PathInitializer.launchWithDefaultApplication(f);
            }
        });
        owner.getElementPane().getChildren().add(this);
    }

    String getName()
    {
        return name.getText();
    }

    void setText(String string) {
        name.setText(string);
    }
}
