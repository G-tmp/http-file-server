package com.alpha.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class FilesFilter {

    private FilesFilter() {
    }



//    // TODO - sort by date, size and ignore case sensitivity
//    public static void sort(File[] files, SortBy sort, int reverse) {
//
//        if (sort == SortBy.NAME) {
////            Arrays.sort(files, Comparator.comparingLong(File::getName));
//            Arrays.sort(files, Comparator.comparing(File::getName));
//
//        } else if (sort == SortBy.SIZE) {
//            Arrays.sort(files, Comparator.comparing(File::length));
//        } else if (sort == SortBy.MODIFIED) {
//            Arrays.sort(files, Comparator.comparing(File::lastModified));
//        }
//
//        if (reverse == 1) {
//            Collections.reverse(Arrays.asList(files));
//        }
//
//
//        // sort by directory first
//        Comparator dirFirstComparator = new Comparator() {
//            public int compare(Object o1, Object o2) {
//                File f1 = (File) o1;
//                File f2 = (File) o2;
//                if (f1.isDirectory() && !f2.isDirectory()) {
//                    // Directory before non-directory
//                    return -1;
//                } else if (!f1.isDirectory() && f2.isDirectory()) {
//                    // Non-directory after directory
//                    return 1;
//                } else {
//                    // Alphabetic order otherwise
//                    return f1.compareTo(f2);
//                }
//            }
//        };
//
//        Arrays.sort(files, dirFirstComparator);
//    }


    // Sort file list by file name
    public static void sortByName(File[] files) {
//            Arrays.sort(files, Comparator.comparingLong(File::getName));
        Arrays.sort(files, Comparator.comparing(File::getName));

        // sort by directory first
        Comparator dirFirstComparator = new Comparator() {
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
                    return f1.compareTo(f2);
                }
            }
        };

        Arrays.sort(files, dirFirstComparator);
    }


    public static File[] showHidden(File dir, int showHidden) {
        File[] files = null;

        if (showHidden == 1) {
            files = dir.listFiles();
        } else if (showHidden == 0) {
            files = dir.listFiles((filter) -> {
                return !filter.isHidden();
            });
        }

        return files;
    }

}
