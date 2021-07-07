package com.alpha.handler;

import com.alpha.utils.Reader;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

public class SingleFile {
    private String name;
    private String filename;
    private String type;
    private byte[] data;

    private InputStream in;
    private String boundary;
    private byte[] body;
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
        this.body = Reader.readHttpRequestBody(in, contentLength);
        String s = new String(body, "utf-8");
        String[] split = s.split("\r\n\r\n", 2);

        System.out.println(split[0]);
        System.out.println("########################");
        System.out.println(split[1]);

        String tmp = split[0].substring(split[0].indexOf("name=") + 6);
        this.name = tmp.substring(0, tmp.indexOf("\"; "));

        tmp = split[0].substring(split[0].indexOf("filename=") + 10);
        this.filename = tmp.substring(0, tmp.indexOf("\""));

        tmp = split[0].substring(split[0].indexOf("Content-Type: ") + 14);
        this.type = tmp.substring(0);

        this.data = Arrays.copyOfRange(body, split[0].getBytes().length + 4, contentLength - boundary.length() - 12);

//        System.out.println(name+".");
//        System.out.println(filename+".");
//        System.out.println(type+".");

    }


    public File save(String parent, String child) throws IOException {
        File file = new File(parent, child);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
        return file;
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
