package org.knowbase.file.browser;

import javafx.concurrent.Task;
import org.knowbase.tools.Methods;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileSearch extends Task<List<Path>> {

    private Path in;
    private boolean scanInside;
    private String argument;

    public FileSearch(Path in, boolean scanInside, String argument) {
        this.in = in;
        this.scanInside = scanInside;
        this.argument = argument;
        setOnSucceeded(event -> {
            List<Path> pathList= (List<Path>) event.getSource().getValue();
            pathList.forEach(System.out::println);
        });
    }

    @Override
    protected List<Path> call() {
        List<Path> applicable=new ArrayList<>();
        List<Path> pathList= Methods.getFiles(in,new ArrayList<>());
        pathList.stream().forEach(path -> {
            try {
                if(scanInside) {
                    List<String> strings = Files.readAllLines(path, Charset.defaultCharset());
                    for (String string : strings) {
                        if (string.contains(argument)) {
                            applicable.add(path);
                            break;
                        }
                    }
                }
                else{
                    String filename=path.getFileName().toString();
                    if(filename.contains(argument))
                        applicable.add(path);
                }
            } catch (IOException ignored) {}
        });
        return applicable;
    }
}
