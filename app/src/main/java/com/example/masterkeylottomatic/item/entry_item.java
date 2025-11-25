package com.example.masterkeylottomatic.item;

public class entry_item {
    private String id;
    private String agent;
    private String combo;
    private String bets;
    private String prize;
    private String transcode;
    private String game;
    private String draw;

    public entry_item(String id, String agent, String combo, String bets, String prize, String transcode, String game, String draw) {
        this.id = id;
        this.agent = agent;
        this.combo = combo;
        this.bets = bets;
        this.prize = prize;
        this.transcode = transcode;
        this.game = game;
        this.draw = draw;
    }

    public String getId() { return id; }
    public String getAgent() { return agent; }
    public String getCombo() { return combo; }
    public String getBets() { return bets; }
    public String getPrize() { return prize; }
    public String getTranscode() {return transcode; }
    public String getGame() { return game; }
    public String getDraw() { return draw; }
}
