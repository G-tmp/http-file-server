package com.alpha.handler;

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
        byte[] buf = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (bytesRecvd < contentLength) {
            c = in.read(buf);
            baos.write(buf, 0, c);
            bytesRecvd += c;
        }

        this.bytes = baos.toByteArray();
        String s = new String(bytes, "utf-8");
        String[] split = s.split("\r\n\r\n", 2);

//        System.out.println(split[0]);
//        System.out.println("########################");
//        System.out.println(split[1]);

        String tmp = split[0].substring(split[0].indexOf("name=") + 6);
        this.name = tmp.substring(0, tmp.indexOf("\"; "));

        tmp = split[0].substring(split[0].indexOf("filename=") + 10);
        this.filename = tmp.substring(0, tmp.indexOf("\""));

        tmp = split[0].substring(split[0].indexOf("Content-Type: ") + 14);
        this.type = tmp.substring(0);

        this.data = Arrays.copyOfRange(bytes, split[0].getBytes().length + 4, contentLength - boundary.length() - 12);

//        System.out.println(name+".");
//        System.out.println(filename+".");
//        System.out.println(type+".");

        baos.close();
    }


    public void save(String parent, String child) throws IOException {
        File file = new File(parent, child);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
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
