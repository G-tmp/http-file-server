package com.alpha.response;

import com.alpha.request.Cookie;
import com.alpha.server.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse {
    private String statusMessage;
    private Map<String, String> headers;
    private byte[] body;
    private List<Cookie> cookies;
    private OutputStream out;
    private boolean chunked = false;


    public HttpResponse(OutputStream out) {
        headers = new HashMap<>();
        cookies = new ArrayList<>();
        this.out = out;
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


    public void enableChunked() {
        this.chunked = true;
        headers.put("Transfer-Encoding", "chunked");
    }


    public void send() throws IOException {
        this.sendHeader();
        if (chunked) {
            sendChunkedFin(body);
        } else {
            this.sendBody(body);
        }
    }


    public void sendHeader() throws IOException {
        if (out == null) {
            throw new IOException("socket output stream closed");
        }

        headers.put("Server", HttpServer.SERVER);
        headers.put("Accept-Ranges", "bytes");
        headers.put("Connection", "keep-alive");
        headers.put("Keep-Alive", "timeout=" + HttpServer.TIMEOUT);
        if (chunked) {
            headers.remove("Content-Length");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(HttpServer.PROTOCOL_VERSION).append(" ").append(statusMessage).append("\r\n");

        for (String headerName : headers.keySet()) {
            sb.append(headerName).append(": ").append(headers.get(headerName)).append("\r\n");
        }

        for (Cookie cookie : cookies) {
            sb.append("Set-Cookie: ").append(cookie).append("\r\n");
        }

        sb.append("\r\n");
        out.write(sb.toString().getBytes());

        out.flush();
    }


    public void sendBody(final byte[] b) throws IOException {
        sendBody(b, 0, b.length);
    }


    public void sendBody(final byte[] b, int offset, int len) throws IOException {
        if (out == null) {
            throw new IOException("socket output stream closed");
        }

        out.write(b, offset, len);
        out.flush();
    }


    public void sendChunked(final byte[] b, int offset, int len) throws IOException {
        if (out == null) {
            throw new IOException("socket output stream closed");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(Integer.toHexString(len).getBytes());
            baos.write("\r\n".getBytes());
            baos.write(b, offset, len);
            baos.write("\r\n".getBytes());

            out.write(baos.toByteArray());
        }

        out.flush();
    }


    public void sendChunkedTrailer() throws IOException {
        if (out == null) {
            throw new IOException("socket output stream closed");
        }

        out.write("0\r\n\r\n".getBytes());
        out.flush();
    }


    // to sending html
    public void sendChunkedFin(final byte[] b) throws IOException {
        int remain = b.length;
        int offset = 0;

        while (remain > HttpServer.BUFFER_SIZE) {
            sendChunked(b, offset, HttpServer.BUFFER_SIZE);
            offset += HttpServer.BUFFER_SIZE;
            remain -= HttpServer.BUFFER_SIZE;
        }

        sendChunked(b, offset, remain);
        sendChunkedTrailer();
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
        sb.append(HttpServer.PROTOCOL_VERSION).append(" ").append(Status._307.toString()).append("\r\n");

        for (String headerName : headers.keySet()) {
            sb.append(headerName).append(": ").append(headers.get(headerName)).append("\r\n");
        }

        for (Cookie cookie : cookies) {
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
        cookies.add(cookie);
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
}
