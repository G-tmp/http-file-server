package com.alpha.request.method;

import com.alpha.response.HttpResponse;
import com.alpha.utils.SingleFile;
import com.alpha.request.HttpRequest;
import com.alpha.response.ContentType;
import com.alpha.response.Status;
import com.alpha.server.HttpServer;

import java.io.File;
import java.io.IOException;

public class Post implements Method {
    private HttpRequest request;
    private HttpResponse response;


    public Post(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public void execute() throws IOException {
        SingleFile singleFile = request.parsePost();

        boolean success = singleFile.save(new File(HttpServer.HOME, request.getPath()).getPath());

        String body = null;
        if (success) {
            body = "<h1>" + singleFile.getFilename() + "</h1>";
        }

        response.setStatusCode(Status._200);
        response.enableChunked();
        response.setContentType(ContentType.HTML);
        response.sendHeader();
        assert body != null;
        response.sendChunkedFin(body.getBytes());
    }
}
