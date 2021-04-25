package com.alpha.request;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
    private BufferedReader in;


    public Request(BufferedReader in) {
        this.headers = new HashMap<>();
        this.queryParameters = new HashMap<>();
        this.in = in;
    }


    // parse http request
    public boolean parse() throws IOException {
        // TODO -  blocking
        String initialLine = in.readLine();
        System.out.println(initialLine);
        //  be used to split string
        StringTokenizer tok = new StringTokenizer(initialLine);
        String[] components = new String[3];

        for (int i = 0; i < components.length; i++) {
            if (tok.hasMoreTokens()) {
                components[i] = tok.nextToken();
            } else {
                return false;
            }
        }

        method = components[0];
        fullUrl = URLDecoder.decode(components[1], "utf-8");
        version = components[2];


        // parse a bound of request headers
        while (true) {
            String headerLine = in.readLine();
            if (headerLine.length() == 0) {
                break;
            }

            int separator = headerLine.indexOf(":");
            // invalid header
            if (separator == -1) {
                return false;
            }
            headers.put(headerLine.substring(0, separator), headerLine.substring(separator + 1));
        }

        // parse request query parameters
        if (!components[1].contains("?")) {
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


    public InputStream getBody() throws IOException {
        return new HttpInputStream(in, headers);
    }


    @Override
    public String toString() {
        return method + " " + path + " " + headers.toString();
    }


    // TODO support mutli-value headers
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }


    public String getParameter(String paramName) {
        return queryParameters.get(paramName);
    }


    public String getMethod() {
        return method;
    }


    public String getFullUrl() {
        // contain query parameters
        return fullUrl;
    }


    public String getPath() {
        // do not contain query parameters
        return path;
    }


    public String getVersion() {
        return version;
    }


}
