package com.alpha.utils;


import java.io.*;

/**
 *    http request header and body split by "\r\n\r\n" (double CRLF)
 */
public class HttpRequestParser {

    private HttpRequestParser() {
    }


    public static byte[] parseHttpRequestHeader(InputStream in) throws IOException {

        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[2];
            int read = 0;

            while ((read = in.read(buf)) != -1) {
                baos.write(buf, 0, read);
                if (baos.toString("utf-8").contains("\r\n\r")) {
                    int n = buf[read - 1];
                    while (n != 10) {   // 10 - LF or \n
                        n = in.read();
                    }
                    break;
                }
            }

            return baos.toByteArray();
        }
    }


    public static byte[] parseHttpRequestBody(InputStream in, int contentLength) throws IOException {
        return parseHttpRequestBody(in, contentLength, 1024 * 1000);
    }


    public static byte[] parseHttpRequestBody(InputStream in, int contentLength, int bufSize) throws IOException {
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

}
