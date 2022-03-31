package com.alpha.httpRequest.method;

import com.alpha.utils.SingleFile;
import com.alpha.httpRequest.Request;
import com.alpha.httpResponse.ContentType;
import com.alpha.httpResponse.Response;
import com.alpha.httpResponse.Status;
import com.alpha.server.HttpServer;

import java.io.File;
import java.io.IOException;

public class Post implements Method {
    private Request request;
    private Response response;


    public Post(Request request, Response response){
        this.request = request;
        this.response = response;
    }

    @Override
    public void execute() throws IOException {
        SingleFile singleFile = request.parsePost();

        File path = singleFile.save(new File(HttpServer.HOME, request.getPath()).getPath());

        String body = "<h1>" + singleFile.getFilename() + "</h1>";
        response.setStatusCode(Status._200);
        response.setContentType(ContentType.HTML);
        response.sendHeader();
//        response.setContentLength(body.getBytes().length);
        response.sendChunkedFin(body.getBytes());
    }
}
