package com.example.masterkey_luckythree.item;

import java.math.BigDecimal;

public class home_item {
    private BigDecimal amount;
    private BigDecimal firstdraw;
    private BigDecimal seconddraw;
    private BigDecimal thirddraw;
    private BigDecimal first3Ddraw;
    private BigDecimal second3Ddraw;
    private BigDecimal third3Ddraw;
    private BigDecimal third4Ddraw;
    private BigDecimal hits2pm2Damount;
    private BigDecimal hits5pm2Damount;
    private BigDecimal hits9pm2Damount;
    private BigDecimal hits2pm3Damount;
    private BigDecimal hits5pm3Damount;
    private BigDecimal hits9pm3Damount;
    private BigDecimal hits9pm4Damount;
    private String agent;
    private String group;

    public home_item(BigDecimal amount, BigDecimal firstdraw, BigDecimal seconddraw, BigDecimal thirddraw, BigDecimal first3Ddraw, BigDecimal second3Ddraw, BigDecimal third3Ddraw, BigDecimal third4Ddraw,
                     BigDecimal hits2pm2Damount, BigDecimal hits5pm2Damount, BigDecimal hits9pm2Damount,
                     BigDecimal hits2pm3Damount, BigDecimal hits5pm3Damount, BigDecimal hits9pm3Damount,
                     BigDecimal hits9pm4Damount,
                     String agent, String group) {
        this.amount = amount;
        this.firstdraw = firstdraw;
        this.seconddraw = seconddraw;
        this.thirddraw = thirddraw;
        this.first3Ddraw = first3Ddraw;
        this.second3Ddraw = second3Ddraw;
        this.third3Ddraw = third3Ddraw;
        this.third4Ddraw = third4Ddraw;
        this.hits2pm2Damount = hits2pm2Damount;
        this.hits5pm2Damount = hits5pm2Damount;
        this.hits9pm2Damount = hits9pm2Damount;
        this.hits2pm3Damount = hits2pm3Damount;
        this.hits5pm3Damount = hits5pm3Damount;
        this.hits9pm3Damount = hits9pm3Damount;
        this.hits9pm4Damount = hits9pm4Damount;
        this.agent = agent;
        this.group = group;
    }

    public BigDecimal getAmount() { return amount; }
    public BigDecimal getFirstdraw() { return firstdraw; }
    public BigDecimal getSeconddraw() { return seconddraw; }
    public BigDecimal getThirddraw() { return thirddraw; }
    public BigDecimal getFirst3Ddraw() { return first3Ddraw; }
    public BigDecimal getSecond3Ddraw() { return second3Ddraw; }
    public BigDecimal getThird3Ddraw() { return third3Ddraw; }
    public BigDecimal getThird4Ddraw() { return third4Ddraw; }
    public BigDecimal getHits2pm2Damount() { return hits2pm2Damount; }
    public BigDecimal getHits5pm2Damount() { return hits5pm2Damount; }
    public BigDecimal getHits9pm2Damount() { return hits9pm2Damount; }
    public BigDecimal getHits2pm3Damount() { return hits2pm3Damount; }
    public BigDecimal getHits5pm3Damount() { return hits5pm3Damount; }
    public BigDecimal getHits9pm3Damount() { return hits9pm3Damount; }
    public BigDecimal getHits9pm4Damount() { return hits9pm4Damount; }
    public String getAgent() { return agent; }
    public String getGroup() { return group; }
}
