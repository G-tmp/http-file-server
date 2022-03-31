package com.alpha.httpResponse;

import com.alpha.httpRequest.Cookie;
import com.alpha.server.HttpServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private boolean chunked;


    public Response(OutputStream out) {
        headers = new HashMap<>();
        cookies = new HashMap<>();
        this.out = out;
        chunked = false;
    }


    public void setStatusCode(Status status) {
        this.statusMessage = status.toString();
    }


    public void setContentType(ContentType contentType) {
        this.headers.put("Content-Type", contentType.toString());
    }


    public void setContentLength(long length) {
        this.headers.put("Content-Length", String.valueOf(length));
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

    public void enableChunked(){
        this.chunked = true;
    }


    // TODO - determine file type by file signature
    public ContentType guessContentType(String url) {
        if (!url.contains(".")) {
            this.headers.put("Content-Type", ContentType.TXT.toString());
            return ContentType.TXT;
        }

        String ext = url.substring(url.lastIndexOf(".") + 1);
        try {
            ContentType type = ContentType.valueOf(ext.toUpperCase());
            this.headers.put("Content-Type", type.toString());
            return type;
        } catch (IllegalArgumentException e) {
            // Http response not add "Content-Type" header
            // Browser will automatic check type
            // So do nothing
            return null;
        }
    }


    public void send() throws IOException {
        this.sendHeader();
        this.sendBody(body);
    }


    public void sendHeader() throws IOException {
        if (out == null) {
            throw new IOException("socket output stream closed");
        }

        headers.put("Server", HttpServer.SERVER);
        headers.put("Accept-Ranges", "bytes");
        headers.put("Connection", "keep-alive");
        headers.put("Keep-Alive", "timeout=" + HttpServer.TIMEOUT);
        if (chunked){
            headers.put("Transfer-Encoding", "chunked");
            headers.remove("Content-Length");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(HttpServer.PROTOCOL_VERSION).append(" ").append(statusMessage).append("\r\n");

        for (String headerName : headers.keySet()) {
            sb.append(headerName).append(": ").append(headers.get(headerName)).append("\r\n");
        }

        for (String cookie : cookies.values()) {
            sb.append("Set-Cookie: ").append(cookie).append("\r\n");
        }

        sb.append("\r\n");
        out.write(sb.toString().getBytes());

        out.flush();
    }


    public void sendBody(byte[] b) throws IOException {
        sendBody(b, 0, b.length);
    }


    public void sendBody(byte[] b, int offset, int len) throws IOException {
        if (out == null) {
            throw new IOException("socket output stream closed");
        }

        out.write(b, offset, len);
        out.flush();
    }


    public void sendChunked(byte[] b, int offset, int len) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(Integer.toHexString(len - offset).getBytes());
        baos.write("\r\n".getBytes());
        baos.write(b, offset, len);
        baos.write("\r\n".getBytes());

        out.write(baos.toByteArray());
        out.flush();
    }


    // to send html
    public void sendChunkedFin(byte[] b) throws IOException {
        sendChunked(b, 0, b.length);
        sendChunkedTrailer();
    }


    public void sendChunkedTrailer() throws IOException {
        out.write("0\r\n\r\n".getBytes());
        out.flush();
    }


    public void redirect(String path) throws IOException {
        if (out == null) {
            throw new IOException("socket output stream closed");
        }

        headers.put("Server", HttpServer.SERVER);
        headers.put("Accept-Ranges", "bytes");
        headers.put("Location", path);
        headers.put("Connection", "close");

        StringBuilder sb = new StringBuilder();
        sb.append(HttpServer.PROTOCOL_VERSION).append(" ").append(Status._302.toString()).append("\r\n");

        for (String headerName : headers.keySet()) {
            sb.append(headerName).append(": ").append(headers.get(headerName)).append("\r\n");
        }

        for (String cookie : cookies.values()) {
            sb.append("Set-Cookie: ").append(cookie).append("\r\n");
        }

        sb.append("\r\n");
        out.write(sb.toString().getBytes());

        out.flush();
    }


    @Override
    public String toString() {
        return new String(body);
    }
}
