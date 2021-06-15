package com.alpha.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class SingleFile {
    private String name;
    private String filename;
    private String type;
    private byte[] data;

    private InputStream in;
    private String boundary;
    private byte[] bytes;
    private int contentLength;


    public SingleFile(InputStream in, Map<String, String> headers) throws IOException {
        try {
            this.contentLength = Integer.parseInt(headers.get("Content-Length"));
        } catch (NumberFormatException e) {
            throw new IOException("Malformed or missing Content-Length header");
        }

        this.in = in;
        this.boundary = headers.get("Content-Type");
        this.boundary = boundary.substring(boundary.indexOf("----") + 4);

        this.parse();
    }


    private void parse() throws IOException {
        int bytesRecvd = 0;
        int c = 0;
        byte[] buf = new byte[2048];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (bytesRecvd < contentLength) {
            c = in.read(buf);
            baos.write(buf, 0, c);
            bytesRecvd += c;
        }

        this.bytes = baos.toByteArray();
        System.out.println(new String(bytes,"utf-8"));
        baos.close();
    }


    public File save(String parent, String child) {
        return new File(parent, child);
    }

    public String getFilename() {
        return filename;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public String getType() {
        return type;
    }
}
