package com.alpha.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Http request header and body split by "\r\n\r\n"
 * While upload file, data and parameters also split by "\r\n\r\n" in post request body
 */
public class HttpRequestParser {
    private static final byte[] EOF = {'\r', '\n', '\r', '\n'};


    private HttpRequestParser() {
    }

    public static class Range {
        public int start;
        public int end;

        public Range(int i, int j) {
            start = i;
            end = j;
        }

        @Override
        public String toString() {
            return start + " - " + end;
        }
    }

    /**
     * input format: "bytes=200-1000, 2000-6576, 19000-"
     * "bytes=0-499, -500"
     */
    public static ArrayList<Range> parseRange(String range) {
        ArrayList<Range> list = new ArrayList<>();
        String s = range.substring("bytes=".length());
        StringTokenizer reqTok = new StringTokenizer(s, ", ");
        while (reqTok.hasMoreTokens()) {
            String sub = reqTok.nextToken();
            int start, end;

            if (sub.startsWith("-")) {
                start = -1;
                end = Integer.parseInt(sub.replace("-", ""));
                list.add(new Range(start, end));
                continue;
            }

            if (sub.endsWith("-")) {
                end = -1;
                start = Integer.parseInt(sub.replace("-", ""));
                list.add(new Range(start, end));
                continue;
            }

            start = Integer.parseInt(sub.split("-")[0]);
            end = Integer.parseInt(sub.split("-")[1]);

            list.add(new Range(start, end));
        }

        return list;
    }


    /**
     * Stop at double CRLF
     * return a bytes array
     */
    public static byte[] parse(InputStream in) throws IOException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[2];
            int read = 0;
            int n;

            while ((read = in.read(buf)) != -1) {
                baos.write(buf, 0, read);

                if (reachEnd(baos.toByteArray())) {
                    n = buf[read - 1];
                    while (n != '\n') {
                        n = in.read();
                        baos.write(n);
                    }

                    break;
                }
            }

            return baos.toByteArray();
        }
    }


    // detect byte array weather reach double CRLF
    private static boolean reachEnd(byte[] bytes) {
        int len = bytes.length;
        if (bytes[len - 1] != '\r' && bytes[len - 1] != '\n')
            return false;

        // detect "\r\n\r"
        if (bytes[len - 1] == '\r') {
            for (int i = 1; i <= 3; i++) {
                if (bytes[len - i] != EOF[3 - i])
                    return false;
            }
        }

        // detect "\r\n\r\n"
        if (bytes[len - 1] == '\n') {
            for (int i = 1; i <= 4; i++) {
                if (bytes[len - i] != EOF[4 - i])
                    return false;
            }
        }

        return true;

        // the simpler detect way
//        return bytes.toString().contains("\r\n\r");
    }
}
