package com.alpha.handler;

import com.alpha.request.Cookie;
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


    public static void doPost(Request request, Response response) throws IOException {
        SingleFile singleFile = request.parsePost();

        File path = singleFile.save(new File(HOME, request.getPath()).getPath(), singleFile.getFilename());
        System.out.println(path);

        String body = "success";
        response.setStatusCode(Status._200);
        response.setContentType(ContentType.HTML);
        response.addBody(body);
        response.setContentLength(body.getBytes().length);
        response.send();
    }


    public static void doGet(Request request, Response response) throws IOException {
        Status statusCode = statusCode(request.getPath());

        switch (statusCode) {
            case _404:
                response.setStatusCode(Status._404);
                response.addBody("");
                response.setContentLength(0);
                response.send();
                break;
            case _401:
                response.setStatusCode(Status._401);
                response.addBody("");
                response.setContentLength(0);
                response.send();
                break;
            case _302:
                response.redirect(request.getPath() + "/");
                break;
            case _200:
                File file = new File(HOME, request.getPath());
                // html
                if (file.isDirectory()) {
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
                        html = mappingLocal(request.getPath(), false);
                    } else if (c.equals("true")) {
                        html = mappingLocal(request.getPath(), true);
                    }

                    response.setStatusCode(Status._200);
                    response.setContentType(ContentType.HTML);
                    response.addBody(html);
                    response.setContentLength(html.getBytes().length);
                    response.send();
                    break;
                } else {
                    File localFile = new File(HOME, request.getPath());
                    try (FileInputStream fis = new FileInputStream(localFile);
                         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                        int read = 0;
                        byte[] buf = new byte[1024 * 1000];
                        while ((read = fis.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                        int length = baos.toByteArray().length;

                        String isDownload = request.getParameter("download");
                        if (isDownload != null && "1".equals(isDownload)) {
                            response.addHeader("Content-Disposition", "attachment; filename=\"" + localFile.getName() + "\"");
                        }

                        // 206
                        String range = request.getHeader("Range");
                        if (range != null && range.contains("bytes")) {
                            int start = Integer.parseInt(range.substring(range.indexOf("=") + 1, range.indexOf("-")));
                            response.setStatusCode(Status._206);
                            // TODO - return partial bytes not all
                            response.addHeader("Content-Range", String.format("bytes 0-%d/%d", length - 1, length));
                            response.guessContentType(request.getPath());
                            response.addBody(baos.toByteArray());
                            response.setContentLength(length);
                            response.send();
                            return;
                        }

                        response.setStatusCode(Status._200);
                        response.addHeader("Accept-Ranges", "bytes");
                        response.guessContentType(request.getPath());
                        response.addBody(baos.toByteArray());
                        response.setContentLength(length);
                        response.send();
                        break;
                    }
                }
            default:
                throw new IOException("unhandled status code");
        }
    }


    private static Status statusCode(String path) {
        File file = new File(HOME, path);
        System.out.println(file);

        if (!file.exists()) {
            return Status._404;
        }

        if (!file.canRead()) {
            return Status._401;
        }

        if (file.isDirectory()) {
            if (!path.endsWith("/")) {
                return Status._302;
            } else

                return Status._200;
        } else {
            // is file
            return Status._200;
        }
    }


    private static String mappingLocal(String path) throws UnsupportedEncodingException {
        return mappingLocal(path, false);
    }


    private static String mappingLocal(String path, boolean showHidden) throws UnsupportedEncodingException {
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

        // TODO - sort by date, size and ignore case sensitivity
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
