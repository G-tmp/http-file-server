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


    public static void doPost(Request request, Response response) throws IOException {
        SingleFile singleFile = request.parsePost();

        File path = singleFile.save(new File(HOME, request.getPath()).getPath(), singleFile.getFilename());
        System.out.println(path);

        response.setStatusCode(Status._200);
        response.setContentType(ContentType.HTML);
        response.addBody("success");
        response.send();
    }


    public static void doGet(Request request, Response response) throws IOException {
        MethodHandlerType type = checkType(request.getPath());

        switch (type) {
            case redirect:
                response.redirect(request.getPath() + "/");
                break;
            case permission_denied:
                response.setStatusCode(Status._401);
                response.addBody("");
                response.send();
                break;
            case not_found:
                response.setStatusCode(Status._404);
                response.addBody("");
                response.send();
                break;
            case html:
                // check parameter
                String showHidden = request.getParameter("showHidden");
                if (showHidden == null) {
                    // do nothing
                } else if (showHidden.equals("0")) {
                    Cookie cookie1 = new Cookie("showHidden", "false").setPath("/").setMaxAge(60 * 60);
                    response.addCookie(cookie1);
                    response.redirect(request.getPath());
                } else if (showHidden.equals("1")) {
                    Cookie cookie1 = new Cookie("showHidden", "true").setPath("/").setMaxAge(60 * 60);
                    response.addCookie(cookie1);
                    response.redirect(request.getPath());
                }


                // check cookie
                String c = request.getCookie("showHidden");
                String html = null;
                if (c == null || c.equals("false")) {
                    html = localMapping(request.getPath(), false);
                } else if (c.equals("true")) {
                    html = localMapping(request.getPath(), true);
                }

                response.setStatusCode(Status._200);
                response.setContentType(ContentType.HTML);
                response.addBody(html);
                response.send();
                break;
            case file:
                File file = new File(HOME, request.getPath());
                try (FileInputStream fis = new FileInputStream(file);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                    int read = 0;
                    byte[] buf = new byte[4096];
                    while ((read = fis.read(buf)) != -1) {
                        baos.write(buf, 0, read);
                    }

                    String isDownload = request.getParameter("download");
                    if (isDownload != null && "1".equals(isDownload)) {
                        response.addHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                    }
                    response.setStatusCode(Status._200);
                    response.guessContentType(request.getPath());
                    response.addBody(baos.toByteArray());
                    response.send();
                    break;
                }
        }

    }


    private static MethodHandlerType checkType(String path) {
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
        html.append("<meta name=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        html.append("<title>").append(path).append("</title>\n</head>\n");
        html.append("<body>\n").append("<h1>Directory listing for ").append(path).append("</h1>\n");
        if (showHidden) {
            html.append("<a href=\"?showHidden=0\"><button>Show Hidden Files</button></a>&#10004;<p>"); // show
        } else {
            html.append("<a href=\"?showHidden=1\"><button>Show Hidden Files</button></a>&#10007;<p>"); // hidden
        }
        html.append("<form  method=\"POST\" enctype=\"multipart/form-data\">\n");
//        html.append("<input type=\"text\" name=\"p1\" required=\"required\"> >>");
        html.append("<input type=\"file\" name=\"file\" required=\"required\"> >>");
        html.append("<button type=\"submit\">Upload</button>\n</form>\n");
        html.append("<hr>\n");

        if ("/".equals(path)) {
            html.append("/");
        } else {
            String parentPath = file.getParent().replace(HOME, "") + "/";
            html.append("<a href=\"").append(parentPath).append("\">").append("Parent Directory").append("</a>");
        }
        html.append("<ul>\n");

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
