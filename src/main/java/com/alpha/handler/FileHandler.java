//package com.alpha.handler;
//
//import com.alpha.response.ContentType;
//import com.alpha.request.Request;
//import com.alpha.response.Response;
//import com.alpha.response.Status;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//
//public class FileHandler implements Handler {
//
//    @Override
//    public void handle(Request request, Response response) throws IOException {
//        try {
//            FileInputStream file = new FileInputStream(request.getPath().substring(1));
//
//            response.setStatusCode(Status._200);
//            response.setContentType(ContentType.HTML);
//
//            // TODO this is slow
//            int c;
//            StringBuffer buf = new StringBuffer();
//            while ((c = file.read()) != -1) {
//                buf.append((char) c);
//            }
//
//            response.addBody(buf.toString());
//            response.setContentLength(buf.toString().getBytes().length);
//        } catch (FileNotFoundException e) {
//            response.setStatusCode(Status._404);
//        }
//    }
//}
