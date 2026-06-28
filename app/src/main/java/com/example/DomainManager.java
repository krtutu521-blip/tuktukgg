package com.example;

public class DomainManager {
    public static final String DOMAIN_ID = "3377";

    public static String appendDomainId(String url) {
        if (url == null) return null;
        String param = "domainId=" + DOMAIN_ID;
        if (url.contains("?")) {
            if (!url.contains("domainId=")) {
                return url + "&" + param;
            }
        } else {
            return url + "?" + param;
        }
        return url;
    }
}
