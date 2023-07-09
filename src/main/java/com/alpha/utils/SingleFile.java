package com.alpha.utils;

import com.alpha.server.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * only support upload one file, but no size limit
 * <p>
 * ***** HTTP post request format below *****
 * POST / HTTP/1.1
 * Content-Type: multipart/form-data; boundary=----WebKitFormBoundarydGnETrh9DhBD8Hlf
 * Content-Length: 55555
 * \r\n
 * ------WebKitFormBoundarydGnETrh9DhBD8Hlf
 * Content-Disposition: form-data; name="file"; filename="1234.png"
 * Content-Type: image/png
 * \r\n
 * [data]
 * ------WebKitFormBoundarydGnETrh9DhBD8Hlf--
 */
public class SingleFile implements Constants {
    private String boundary;
    private InputStream in;
    private long contentLen;

    private String filename;
    private long fileLen;
    private boolean isExist;


    public SingleFile(InputStream in, Map<String, String> headers) throws IOException {
        try {
            this.contentLen = Long.parseLong(headers.get("Content-Length"));
        } catch (NumberFormatException e) {
            throw new IOException("Malformed or missing Content-Length header");
        }

        this.in = in;
        String tmp = headers.get("Content-Type");
        this.boundary = tmp.substring(tmp.indexOf("boundary=----") + 13);

        this.parse();
    }


    private void parse() throws IOException {
        byte[] parameterPairs = HttpRequestParser.parse(in);
        String s = new String(parameterPairs, StandardCharsets.UTF_8);

        this.fileLen = contentLen - parameterPairs.length - 12 - boundary.length();
        String split = s.split("\r\n")[1];

        this.filename = split.substring(split.indexOf("filename=\"") + 10, split.lastIndexOf("\""));
//        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        Date date = new Date();
//        this.filename = dateFormat.format(date) + "_" + split.substring(split.indexOf("filename=\"") + 10, split.lastIndexOf("\""));
    }


    public boolean save(String dir) throws IOException {
        File file = new File(dir, filename);
        isExist = file.exists();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

        long read = 0;
        int c;
        byte[] b = new byte[BUFFER_SIZE];
        while (read < fileLen) {
            c = in.read(b);
            if (c == -1)
                break;

            if (read + c > fileLen) {
                bos.write(b, 0, (int) (fileLen - read));
            } else {
                bos.write(b, 0, c);
            }

            read += c;
        }

        bos.close();
        if (file.length() != fileLen) {
            file.delete();
            return false;
        }

        return true;
    }


    public String getFilename() {
        return filename;
    }


    public long getFileLen() {
        return fileLen;
    }


    public boolean isExist() {
        return isExist;
    }

}
