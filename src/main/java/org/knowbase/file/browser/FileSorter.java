package org.knowbase.file.browser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileSorter implements Comparator<Path> {
    @Override
    public int compare(Path o1, Path o2) {
        if(FileBrowser.fileSort==FileSort.ALPHABETIC) {
            if (Files.isDirectory(o1) && !Files.isDirectory(o2))
                return -1;
            if (!Files.isDirectory(o1) && Files.isDirectory(o2))
                return 1;
            return o1.toString().compareTo(o2.toString());
        }
        else if(FileBrowser.fileSort==FileSort.EXTENSION)
        {

            if (Files.isDirectory(o1) && !Files.isDirectory(o2))
                return -1;
            if (!Files.isDirectory(o1) && Files.isDirectory(o2))
                return 1;
            if(!Files.isDirectory(o1) && !Files.isDirectory(o2))
            {
                String string=o1.getFileName().toString();
                String s2=o2.getFileName().toString();
                if(string.contains(".") && s2.contains("."))
                {
                    String ext=string.substring(string.lastIndexOf('.')+1);
                    String e2=s2.substring(s2.lastIndexOf('.')+1);
                    return ext.compareTo(e2);
                }
                return string.compareTo(s2);
            }
            return o1.toString().compareTo(o2.toString());
        }
        return 0;
    }
}
