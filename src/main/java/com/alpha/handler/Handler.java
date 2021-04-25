package com.alpha.handler;

import com.alpha.request.Request;
import com.alpha.response.Response;

import java.io.IOException;



public interface Handler {

    public void handle(Request request, Response response) throws IOException;

}
