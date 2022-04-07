package com.alpha.request;


public class Cookie {
    private String name;
    private String value;
    private Integer maxAge = -1;
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
        sb.append(name).append("=").append(value);

        if (maxAge != null) {
            sb.append("; Max-Age=").append(maxAge);
        }
        if (domain != null) {
            sb.append("; Domain=").append(domain);
        }

        if (path != null) {
            sb.append("; Path=").append(path);
        }

        if (secure) {
            sb.append("; Secure");
        }

        if (httpOnly) {
            sb.append("; HttpOnly");
        }

        if (sameSite != null) {
            sb.append("; SameSite=").append(sameSite);
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
