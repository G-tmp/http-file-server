package com.alpha.httpRequest;


import com.alpha.utils.HttpRequestParser;
import com.alpha.utils.SingleFile;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Request {
    private String method;
    private String path;    // do not contain query parameters
    private String fullUrl;     // maybe contain query parameters
    private String version;
    private Map<String, String> headers;    // store header name and value
    private Map<String, String> queryParameters;
    private Map<String, String> cookies;
    private InputStream in;


    public Request(InputStream in) {
        this.headers = new HashMap<>();
        this.queryParameters = new HashMap<>();
        this.cookies = new HashMap<>();
        this.in = new BufferedInputStream(in);
    }


    public boolean parse() throws IOException {
        byte[] data = HttpRequestParser.parseHttpRequestHeader(in);

        String requestHeaders = new String(data, "utf-8");
        StringTokenizer reqTok = new StringTokenizer(requestHeaders, "\r\n");
//        System.out.println(requestHeaders);

        // parse initial line
        String initialLine = reqTok.nextToken();
        System.out.println(initialLine);
        StringTokenizer initTok = new StringTokenizer(initialLine);
        String[] components = new String[3];
        for (int i = 0; i < components.length; i++) {
            if (initTok.hasMoreTokens()) {
                components[i] = initTok.nextToken();
            } else {
                return false;
            }
        }
        method = components[0];
        path = fullUrl = URLDecoder.decode(components[1], "utf-8");
        version = components[2];


        // parse request headers
        while (reqTok.hasMoreTokens()) {
            String headerLine = reqTok.nextToken();
//            System.out.println(headerLine);
            if (headerLine.length() == 0) {
                break;
            }

            int separator = headerLine.indexOf(":");
            // invalid header
            if (separator == -1) {
                return false;
            }

            String headerName = headerLine.substring(0, separator);
            String headerValue = headerLine.substring(separator + 2);

            if ("Cookie".equals(headerName)) {
                parseCookies(headerValue);
                continue;
            }

            headers.put(headerName, headerValue);
        }

        // parse request query parameters
        if (!fullUrl.contains("?")) {
            path = fullUrl;
        } else {
            path = fullUrl.substring(0, fullUrl.indexOf("?"));
            parseQueryParameters(fullUrl.substring(fullUrl.indexOf("?") + 1));
        }

        return true;
    }


    private void parseQueryParameters(String queryString) {
        for (String parameter : queryString.split("&")) {
            int separator = parameter.indexOf('=');
            if (separator > -1) {
                queryParameters.put(parameter.substring(0, separator),
                        parameter.substring(separator + 1));
            } else {
                queryParameters.put(parameter, null);
            }
        }
    }


    private void parseCookies(String cookieString) {
        String[] cookiePairs = cookieString.split("; ");
        for (String s : cookiePairs) {
            int equal = s.indexOf("=");
            cookies.put(s.substring(0, equal), s.substring(equal + 1));
        }
    }


    public SingleFile parsePost() throws IOException {
        return new SingleFile(in, headers);
    }


    @Override
    public String toString() {
        return method + " " + path + " " + headers.toString();
    }


    public String getCookie(String key) {
        return cookies.get(key);
    }


    public Map<String, String> getCookies() {
        return cookies;
    }


    // TODO support mutli-value headers
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }


    public Map<String, String> getHeaders() {
        return headers;
    }


    public String getParameter(String paramName) {
        return queryParameters.get(paramName);
    }


    public Map<String, String> getParameters() {
        return queryParameters;
    }


    public String getMethod() {
        return method;
    }


    public String getFullUrl() {
        return fullUrl;
    }


    public String getPath() {
        return path;
    }


    public String getVersion() {
        return version;
    }

}
