package com.example.masterkeylottomatic.item;

public class game_item {
    private String gameName;
    private String draw2pm;
    private String draw5pm;
    private String draw9pm;
    private String hits2pm;
    private String hits5pm;
    private String hits9pm;
    private String gross;
    private boolean is4DGame;

    public game_item(String gameName, String draw2pm, String draw5pm, String draw9pm,
                     String hits2pm, String hits5pm, String hits9pm, String gross) {
        this.gameName = gameName != null ? gameName : "GAME";
        this.draw2pm = draw2pm != null ? draw2pm : "0.00";
        this.draw5pm = draw5pm != null ? draw5pm : "0.00";
        this.draw9pm = draw9pm != null ? draw9pm : "0.00";
        this.hits2pm = hits2pm != null ? hits2pm : "0.00";
        this.hits5pm = hits5pm != null ? hits5pm : "0.00";
        this.hits9pm = hits9pm != null ? hits9pm : "0.00";
        this.gross = gross != null ? gross : "0.00";
        this.is4DGame = gameName.equalsIgnoreCase("4D GAME");
    }

    public String getGameName() { return gameName; }
    public String getDraw2pm() { return draw2pm; }
    public String getDraw5pm() { return draw5pm; }
    public String getDraw9pm() { return draw9pm; }
    public String getHits2pm() { return hits2pm; }
    public String getHits5pm() { return hits5pm; }
    public String getHits9pm() { return hits9pm; }
    public String getGross() { return gross; }

    public String calculateTotalHits() {
        try {
            double h2pm = Double.parseDouble(hits2pm.replace("₱", ""));
            double h5pm = Double.parseDouble(hits5pm.replace("₱", ""));
            double h9pm = Double.parseDouble(hits9pm.replace("₱", ""));
            return String.format("₱%.2f", h2pm + h5pm + h9pm);
        } catch (NumberFormatException e) {
            return "₱0.00";
        }
    }

    public boolean is4DGame() {
        return is4DGame;
    }
}
