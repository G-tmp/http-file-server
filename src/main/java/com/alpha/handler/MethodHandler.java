package com.alpha.handler;

import com.alpha.request.Request;
import com.alpha.response.ContentType;
import com.alpha.response.Response;
import com.alpha.response.Status;

import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;


public class MethodHandler {

    private enum MethodHandlerType {
        file,
        html,
        permission_denied,
        not_found,
        redirect;
    }


    private final static String HOME = System.getProperty("user.home");


    private MethodHandler() {
    }


    // TODO - parse http response and save file
    public static void doPost(Request request, Response response) throws IOException {
//        request.parse();
//
//        InputStream bodyIS = request.getBody();
//        int ch = 0;
//        while ((ch = bodyIS.read()) != -1) {
//            System.out.println((char) ch);
//        }

    }


    public static void doGet(Request request, Response response) throws IOException {
        MethodHandlerType type = checkType(request.getPath());

        switch (type) {
            case redirect:
                response.setStatusCode(Status._301);
                response.addHeader("Location", request.getPath() + "/");
                response.send();
                break;
            case permission_denied:
                response.setStatusCode(Status._403);
                response.addBody("");
                response.send();
                break;
            case not_found:
                response.setStatusCode(Status._404);
                response.addBody("");
                response.send();
                break;
            case html:
                // TODO - cookie and query parameter
                String html = localMapping(request.getPath());
                response.setStatusCode(Status._200);
                response.setContentType(ContentType.HTML);
                response.addBody(html);
                response.send();
                break;
            case file:
                File file = new File(HOME, request.getPath());
                int length = (int) file.length();
                byte[] array = new byte[length];
                InputStream in = new FileInputStream(file);
                int offset = 0;
                while (offset < length) {
                    int count = in.read(array, offset, (length - offset));
                    offset += count;
                }
                String isDownload = request.getParameter("download");
                if (isDownload != null && "1".equals(isDownload)) {
                    response.addHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                }
                response.setStatusCode(Status._200);
                response.guessContentType(request.getPath());
                response.addBody(array);
                response.send();
                break;
        }

    }


    private static MethodHandlerType checkType(String path) throws UnsupportedEncodingException {
        File file = new File(HOME, path);
        System.out.println(file);

        if (!file.exists()) {
            return MethodHandlerType.not_found;
        }

        if (!file.canRead()) {
            return MethodHandlerType.permission_denied;
        }

        if (file.isDirectory()) {
            if (!path.endsWith("/")) {
                return MethodHandlerType.redirect;
            }

            return MethodHandlerType.html;
        } else {
            // is file
            return MethodHandlerType.file;
        }
    }


    private static String localMapping(String path) throws UnsupportedEncodingException {
        return localMapping(path, false);
    }


    private static String localMapping(String path, boolean showHidden) throws UnsupportedEncodingException {
        File file = new File(HOME, path);
        StringBuffer html = new StringBuffer();
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        html.append("<html>\n<head>\n");
        html.append("<meta name=\"Content-Type\" content=\"text/html;charset=utf-8\">\n");
        html.append("<title>").append(path).append("</title>\n</head>\n");
        html.append("<body>\n").append("<h1>Index of ").append(path).append("</h1>\n");
        html.append("<form  method=\"POST\" enctype=\"multipart/form-data\">\n");
        html.append("<input type=\"text\" name=\"p1\" required=\"required\"> >>");
        html.append("<input type=\"file\" name=\"file\" required=\"required\"> >>");
        html.append("<button type=\"submit\">Upload</button>\n</form>\n");
        html.append("<hr>\n").append("<ul>\n");
        // TODO
        if (file.equals(new File(HOME)))
            html.append("<p>");
        else
            html.append("<a href=\"").append(file.getParent().replace(HOME, "")).append("\">").append("Parent Directory").append("</a>").append("<p>");

        File[] files = null;
        if (showHidden) {
            files = file.listFiles();
        } else {
            files = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.isHidden();
                }
            });
        }

        // TODO - sort ignore case sensitivity
        Arrays.sort(files);
        for (File subfile : files) {
            String displayName = subfile.getName();
            String link = URLEncoder.encode(subfile.getName(), "UTF-8");

            if (subfile.isDirectory()) {
                displayName += "/";
                link += "/";
                String element = String.format("<a href=\"%s\">%s</a>", link, displayName);
                html.append("<li>").append(element).append("</li>\n");
            } else if (subfile.isFile()) {
                String element = String.format("<a href=\"%s\">%s</a>", link, displayName);
                String download = String.format("<a href=\"%s\">%s</a>", link + "?download=1", "DL");
                html.append("<li>").append(element).append("&nbsp;&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;&nbsp;").append(download).append("</li>\n");
            } else {
                // symbol link
                displayName = displayName + "@";
                String element = String.format("<a href=\"%s\">%s</a>", link, displayName);
                html.append("<li>").append(element).append("</li>\n");
            }

        }
        html.append("</ul>\n<hr>\n</body>\n</html>");

        return String.valueOf(html);
    }
}
