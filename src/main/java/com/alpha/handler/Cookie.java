package com.alpha.handler;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Cookie {
    private String name;
    private String value;
    private Date expires;
    private Integer maxAge;
    private String domain;
    private String path;
    private boolean secure;
    private boolean httpOnly;
    private String sameSite;


    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name + "=" + value);

        if (expires != null) {
            SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
            sb.append("; Expires=" + fmt.format(expires) + " GMT");
        }

        if (maxAge != null) {
            sb.append("; Max-Age=" + maxAge);
        }
        if (domain != null) {
            sb.append("; Domain=" + domain);
        }

        if (path != null) {
            sb.append("; Path=" + path);
        }

        if (secure) {
            sb.append("; Secure");
        }

        if (httpOnly) {
            sb.append("; HttpOnly");
        }

        if (sameSite != null) {
            sb.append("; SameSite=" + sameSite);
        }

        return String.valueOf(sb);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Cookie setValue(String value) {
        this.value = value;
        return this;
    }

    public Date getExpires() {
        return expires;
    }

    public Cookie setExpires(Date expires) {
        this.expires = expires;
        return this;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public Cookie setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public Cookie setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Cookie setPath(String path) {
        this.path = path;
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public Cookie setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public Cookie setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    public String getSameSite() {
        return sameSite;
    }

    public Cookie setSameSite(String sameSite) {
        this.sameSite = sameSite;
        return this;
    }

}
