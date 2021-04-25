package com.alpha.handler;

import com.alpha.request.Request;
import com.alpha.response.ContentType;
import com.alpha.response.Response;
import com.alpha.response.Status;

import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;


public class MethodHandler {

    private final static String HOME = System.getProperty("user.home");

    private MethodHandler() {
    }


    // TODO - parse http response and save file
    public static void doPost(Request request, Response response) {
        InputStream bodyIs = null;

        try {
            bodyIs = request.getBody();
            bodyIs.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void doGet(Request request, Response response) throws IOException {

        String res = checkStatus(request.getPath());

        if (res == null) {
            // TODO - read file
            File file = new File(HOME, request.getPath());
            int length = (int) file.length();
            byte[] array = new byte[length];
            InputStream in = new FileInputStream(file);
            int offset = 0;
            while (offset < length) {
                int count = in.read(array, offset, (length - offset));
                offset += count;
            }
            response.setStatusCode(Status._200);
            response.guessContentType(request.getPath());
            response.addBody(array);
            response.send();
        } else if ("301".equals(res)) {
            response.setStatusCode(Status._301);
            response.addHeader("Location", request.getPath() + "/");
            response.send();
        } else if ("403".equals(res)) {
            response.setStatusCode(Status._403);
            response.addBody("");
            response.send();
        } else if ("404".equals(res)) {
            response.setStatusCode(Status._404);
            response.addBody("");
            response.send();
        } else if (res.contains("html")) {
            // html
            response.setStatusCode(Status._200);
            response.setContentType(ContentType.HTML);
            response.addBody(res);
            response.send();
        } else {
            System.err.println("error");
        }

    }


    private static String checkStatus(String path) throws UnsupportedEncodingException {
        File file = new File(HOME, path);
        System.out.println(file);
        if (!file.exists()) {
            return "404";
        }

        if (!file.canRead()) {
            return "403";
        }


        if (file.isDirectory()) {
            if (!path.endsWith("/")) {
                return "301";
            }

            return localMapping(path);
        } else {
            // is file
            return null;
        }
    }


    private static String localMapping(String realPath) throws UnsupportedEncodingException {
        File file = new File(HOME, realPath);
        String title = realPath;
        StringBuffer html = new StringBuffer();
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        html.append("<html>\n<head>\n");
        html.append("<meta name=\"Content-Type\" content=\"text/html;charset=utf-8\">\n");
        html.append("<title>").append(title).append("</title>\n</head>\n");
        html.append("<body>\n").append("<h1>Index of ").append(title).append("</h1>\n");
//            html.append("<form  method=\"POST\" enctype=\"multipart/form-data\">\n");
//            html.append("<input type=\"file\" name=\"file\" required=\"required\"> >>");
//            html.append("<button type=\"submit\">Upload</button>\n</form>\n");
        html.append("<hr>\n").append("<ul>\n");

        File[] files = file.listFiles();
        // TODO - sort ignore case sensitivity
        Arrays.sort(files);
        for (File subfile : files) {
            String displayName = subfile.getName();
            String link = URLEncoder.encode(subfile.getName(), "UTF-8");

            if (subfile.isDirectory()) {
                displayName += "/";
                link += "/";
            } else if (subfile.isFile()) {
                // do nothing
            } else {
                // symbol link
                displayName = displayName + "@";
            }
            String element = String.format("<a href=\"%s\">%s</a>", link, displayName);
            html.append("<li>").append(element).append("</li>\n");
        }
        html.append("</ul>\n<hr>\n</body>\n</html>");

        return String.valueOf(html);
    }
}
