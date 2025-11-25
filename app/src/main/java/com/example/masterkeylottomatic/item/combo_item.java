package com.example.masterkeylottomatic.item;

public class combo_item {
    private String combo;
    private String total;
    private String game;
    private String draw;

    public combo_item (String combo, String total, String game, String draw) {
        this.combo = combo;
        this.total = total;
        this.game = game;
        this.draw = draw;
    }

    public String getCombo() { return combo; }
    public String getTotal() { return total; }
    public String getGame() { return game; }
    public String getDraw() { return draw; }
}
