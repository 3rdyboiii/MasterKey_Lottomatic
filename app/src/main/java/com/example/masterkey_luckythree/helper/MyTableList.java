package com.example.masterkey_luckythree.helper;
import java.math.BigDecimal;

public class MyTableList {
    private String name, combo, draw, game, transcode;
    private int ID;
    private BigDecimal total, bets;

    public String getName() {
        return name;
    }

    public void setName(String name) {this.name = name;}
    public BigDecimal getTotal() {
        return total;
    }
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    public String getCombo() { return combo; }
    public void setCombo(String combo) {this.combo = combo;}
    public String getDraw() {return draw; }
    public void setDraw(String draw) {this.draw = draw;}
    public String getGame() {return game;}
    public void setGame(String game) {this.game = game;}
    public BigDecimal getBets() {return bets;}
    public void setBets(BigDecimal bets) {this.bets = bets;}
    public String getTranscode() {return transcode;}
    public void setTranscode(String transcode) {this.transcode = transcode;}
    public int getID() {return ID;}
    public void setID(int ID) {this.ID = ID;}
}
