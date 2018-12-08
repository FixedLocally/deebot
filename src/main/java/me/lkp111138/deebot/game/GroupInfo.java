package me.lkp111138.deebot.game;

public class GroupInfo {
    public final int wait_time;
    public final boolean collect_place;
    public final boolean fry;

    public GroupInfo(int wait_time, boolean collect_place, boolean fry) {
        this.wait_time = wait_time;
        this.collect_place = collect_place;
        this.fry = fry;
    }
}
