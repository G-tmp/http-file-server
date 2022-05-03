package com.alpha.utils;


import java.io.*;

/**
 * Http request header and body split by "\r\n\r\n"
 * While upload file, data and parameters also split by "\r\n\r\n" in post request body
 * <p>
 * <p>
 * <p>
 * POST / HTTP/1.1
 * Content-Type: multipart/form-data; boundary=----WebKitFormBoundarydGnETrh9DhBD8Hlf
 * Content-Length: 55555
 * <p>
 * ------WebKitFormBoundarydGnETrh9DhBD8Hlf
 * Content-Disposition: form-data; name="file"; filename="1234.png"
 * Content-Type: image/png
 * <p>
 * [data]
 * ------WebKitFormBoundarydGnETrh9DhBD8Hlf--
 */
public class HttpRequestParser {
    private static final byte[] EOF = {'\r', '\n', '\r', '\n'};


    private HttpRequestParser() {
    }

    /**
     * return bytes array before double CRLF
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


    // detect byte array weather reach EOF
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
