package me.lkp111138.deebot.game;

public class GroupInfo {
    final int waitTime;
    final boolean collectPlace;
    final boolean fry;

    public GroupInfo(int waitTime, boolean collect_place, boolean fry) {
        this.waitTime = waitTime;
        this.collectPlace = collect_place;
        this.fry = fry;
    }
}
