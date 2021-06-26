package com.alpha.utils;

import java.io.*;

public class Reader {

    private Reader() {
    }


    public static byte[] readHttpRequestHeader(InputStream in) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buf = new byte[2];
            int read = 0;

            while ((read = in.read(buf)) != -1) {
                baos.write(buf, 0, read);
                if (baos.toString("utf-8").contains("\r\n\r")) {
                    int n = buf[read - 1];
                    while (n != 10) {   // 10 - LF
                        n = in.read();
                    }
                    break;
                }
            }

            return baos.toByteArray();
        }
    }


    public static byte[] readHttpRequestBody(InputStream in, int contentLength) throws IOException {
        return readHttpRequestBody(in, contentLength, 1024 * 1000);
    }


    public static byte[] readHttpRequestBody(InputStream in, int contentLength, int bufSize) throws IOException {
        int totalRead = 0;
        int read = 0;
        byte[] buf = new byte[bufSize];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            while (totalRead < contentLength) {
                read = in.read(buf);
                baos.write(buf, 0, read);
                totalRead += read;
            }

            return baos.toByteArray();
        }
    }


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
