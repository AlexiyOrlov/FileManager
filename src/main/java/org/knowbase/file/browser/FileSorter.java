package org.knowbase.file.browser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileSorter implements Comparator<Path> {
    @Override
    public int compare(Path o1, Path o2) {
        if(Files.isDirectory(o1) && !Files.isDirectory(o2))
            return -1;
        if(!Files.isDirectory(o1) && Files.isDirectory(o2))
            return 1;
        return 0;
    }
}
