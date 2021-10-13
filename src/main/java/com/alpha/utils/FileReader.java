package com.alpha.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileReader {



    public static byte[] readFile(File file) throws IOException {
        return readFile(file, 1024 * 1000);
    }


    public static byte[] readFile(File file, int bufSize) throws IOException {
        int read = 0;
        byte[] buf = new byte[bufSize];
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            while ((read = fis.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }

            return baos.toByteArray();
        }
    }

}
