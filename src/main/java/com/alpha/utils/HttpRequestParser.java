package com.alpha.utils;


import java.io.*;

/**
 *    Http request header and body split by "\r\n\r\n"
 *    While upload file, data and parameters also split by "\r\n\r\n" in post request body
 *
 *
 *
 *  POST / HTTP/1.1
 *  Content-Type: multipart/form-data; boundary=----WebKitFormBoundarydGnETrh9DhBD8Hlf
 *  Content-Length: 55555
 *
 *  ------WebKitFormBoundarydGnETrh9DhBD8Hlf
 *  Content-Disposition: form-data; name="file"; filename="1234.png"
 *  Content-Type: image/png
 *
 *  [data]
 *  ------WebKitFormBoundarydGnETrh9DhBD8Hlf--
 */
public class HttpRequestParser {

    private HttpRequestParser() {
    }


    /**
     *  return bytes array before double CRLF
     */
    public static byte[] parse(InputStream in) throws IOException {

        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[2];
            int read = 0;
            int n;
            
            while ((read = in.read(buf)) != -1) {
                baos.write(buf, 0, read);
                // TODO   KMP algorithm detect
                if (baos.toString().contains("\r\n\r")) {
                    n = buf[read - 1];
                    while (n != 10) {   // 10 - ascii code of  LF or '\n'
                        n = in.read();
                        baos.write(n);
                    }

                    break;
                }
            }

            return baos.toByteArray();
        }
    }

}
