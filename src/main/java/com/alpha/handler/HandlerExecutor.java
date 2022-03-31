//package com.alpha.handler;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class HandlerExecutor implements Handler {
//
//    private List<Handler> handlers = new ArrayList<>();
//
//
//    public void add(Handler handler) {
//        handlers.add(handler);
//    }
//
//    @Override
//    public void handle() throws IOException {
//
//        for (Handler handler : handlers) {
//            handler.handle();
//        }
//    }
//}
