package com.alpha.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class FilesFilter {

    private FilesFilter() {
    }


    // list directory first and ignore case sensitive
    private final static Comparator DIR_FIRST_AND_UNSENSITIVE_COMP = new Comparator() {
        public int compare(Object o1, Object o2) {
            File f1 = (File) o1;
            File f2 = (File) o2;

            if (f1.isDirectory() && !f2.isDirectory()) {
                // Directory before non-directory
                return -1;
            } else if (!f1.isDirectory() && f2.isDirectory()) {
                // Non-directory after directory
                return 1;
            } else {
                // Alphabetic order otherwise
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        }
    };


    // Sort files order by file name
    public static void sortByFileName(File[] files) {
        Arrays.sort(files, DIR_FIRST_AND_UNSENSITIVE_COMP);
    }


    public static File[] showHidden(File dir, boolean showHidden) {
        File[] files = null;

        if (showHidden) {
            files = dir.listFiles();
        } else {
            files = dir.listFiles((filter) -> {
                return !filter.isHidden();
            });
        }

        return files;
    }

}