package com.example.masterkeylottomatic.item;

public class account_item {
    private String username;
    private String name;
    private String code;
    private String version;
    private String group;

    public account_item(String username, String name, String code, String version, String group) {
        this.username = username;
        this.name = name;
        this.code = code;
        this.version = version;
        this.group = group;
    }

    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getVersion() { return version; }
    public String getGroup() { return group; }
}
