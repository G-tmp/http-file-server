package com.alpha.response;

import com.alpha.request.Cookie;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private String statusMessage;
    private Map<String, String> headers;
    private byte[] body;
    private Map<String, String> cookies;
    private OutputStream out;


    public Response(OutputStream out) {
        headers = new HashMap<>();
        cookies = new HashMap<>();
        this.out = out;
    }


    public void setStatusCode(Status status) {
        this.statusMessage = status.toString();
    }


    public void setContentType(ContentType contentType) {
        this.headers.put("Content-Type", contentType.toString());
    }

    public void setContentLength(int length){
        this.headers.put("Content-Length", String.valueOf(length));
    }


    // TODO - determine file type by file signature
    public void guessContentType(String url) {
        if (!url.contains(".")) {
            this.headers.put("Content-Type", ContentType.TXT.toString());
            return;
        }

        String ext = url.substring(url.lastIndexOf(".") + 1);
        try {
            ContentType type = ContentType.valueOf(ext.toUpperCase());
            this.headers.put("Content-Type", type.toString());
        } catch (IllegalArgumentException e) {
            // Http response not add "Content-Type" header
            // browser will automatic check type
            // So do nothing
        }
    }


    public void addCookie(Cookie cookie) {
        cookies.put(cookie.getName(), cookie.toString());
    }


    public void addHeader(String headerName, String headerValue) {
        this.headers.put(headerName, headerValue);
    }


    public void addBody(String body) {
        addBody(body.getBytes());
    }


    public void addBody(byte[] body) {
        this.body = body;
    }


    public void send() throws IOException {
        headers.put("Connection", "keep-alive");

        out.write(("HTTP/1.1 " + statusMessage + "\r\n").getBytes());
        for (String headerName : headers.keySet())
            out.write((headerName + ": " + headers.get(headerName) + "\r\n").getBytes());

        for (String cookie : cookies.values())
            out.write(("Set-Cookie" + ": " + cookie + "\r\n").getBytes());

        out.write("\r\n".getBytes());
        if (body != null) {
            out.write(body);
        }

        out.flush();
    }


    public void redirect(String path) throws IOException {
        out.write(("HTTP/1.1 " + Status._302.toString() + "\r\n").getBytes());
        out.write(("Location" + ": " + path + "\r\n").getBytes());
//        out.write(("Connection" + ": " + "close" + "\r\n").getBytes());

        for (String cookie : cookies.values()) {
            out.write(("Set-Cookie" + ": " + cookie + "\r\n").getBytes());
        }

        out.write("\r\n".getBytes());

        out.flush();
    }
}
