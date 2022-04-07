package com.alpha.utils;

import com.alpha.server.HttpServer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Only support upload one file
 */
public class SingleFile {
    private String boundary;
    private InputStream in;
    private long contentLen;

    private String filename;
    private long fileLen;


    public SingleFile(InputStream in, Map<String, String> headers) throws IOException {
        try {
            this.contentLen = Long.parseLong(headers.get("Content-Length"));
        } catch (NumberFormatException e) {
            throw new IOException("Malformed or missing Content-Length header");
        }

        this.in = in;
        this.boundary = headers.get("Content-Type");
        this.boundary = boundary.substring(boundary.indexOf("boundary=----") + 13);

        this.parse();
    }


    private void parse() throws IOException {
        byte[] parameterPair = HttpRequestParser.parse(in);
        String s = new String(parameterPair, StandardCharsets.UTF_8);

        String split = s.split("\r\n")[1];
        this.filename = split.substring(split.indexOf("filename=\"") + 10, split.lastIndexOf("\""));
        this.fileLen = contentLen - parameterPair.length - 12 - boundary.length();
    }


    public File save(String dir) throws IOException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        File file = new File(dir, dateFormat.format(date) + "_" + this.filename);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

        long read = 0;
        int c;
        byte[] b = new byte[HttpServer.BUFFER_SIZE];
        while (read < fileLen) {
            c = in.read(b);
            if (c == -1)
                break;

            if (read + c > fileLen)
                bos.write(b, 0, (int) (fileLen - read));
            else
                bos.write(b, 0, c);

            read += c;
        }

        bos.close();
        if (file.length() != fileLen) {
            file.delete();
        }

        return file;
    }


    public String getFilename() {
        return filename;
    }


    public long getFileLen() {
        return fileLen;
    }
}
