package com.example.masterkey_luckythree.item;

public class detail_Item {
    private String username;
    private String name;
    private String code;

    public detail_Item(String username, String name, String code) {
        this.username = username;
        this.name = name;
        this.code = code;
    }

    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getCode() { return code; }
}
