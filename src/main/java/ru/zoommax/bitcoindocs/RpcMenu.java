package ru.zoommax.bitcoindocs;

import java.util.List;

public class RpcMenu {
    private String version;
    private String h3;
    private List<String> urls;
    private List<String> names;

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setH3(String h3) {
        this.h3 = h3;
    }

    public String getH3() {
        return h3;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }

    public String toString() {
        return "h3: " + h3 + " urls: " + urls + " names: " + names;
    }
}
