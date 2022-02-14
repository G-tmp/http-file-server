package com.alpha.utils;

import java.io.*;
import java.util.Map;

/**
 * Only support upload one file
 */
public class SingleFile {
    private String boundary;
    private String filename;
    private long contentLen;
    private long fileLen;
    private InputStream in;


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
        String s = new String(parameterPair, "utf-8");

        String split = s.split("\r\n")[1];
        this.filename = split.substring(split.indexOf("filename=\"") + 10, split.lastIndexOf("\""));
        this.fileLen = contentLen - parameterPair.length - 12 - boundary.length();
    }


    public File save(String parent) throws IOException {
        File file = new File(parent, this.filename);
        FileOutputStream fos = new FileOutputStream(file);

        long read = 0;
        int c;
        byte[] b = new byte[1024 * 16];
        while (read < fileLen) {
            c = in.read(b);
            if (c == -1)
                break;

            if (read + c > fileLen)
                fos.write(b, 0, (int) (fileLen - read));
            else
                fos.write(b, 0, c);

            read += c;
        }

        fos.close();

        return file;
    }


    public String getFilename() {
        return filename;
    }


    public long getFileLen(){
        return fileLen;
    }
}
