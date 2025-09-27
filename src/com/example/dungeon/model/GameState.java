package com.example.dungeon.model;

import java.io.Serializable;

public class GameState implements Serializable {
    private Player player;
    private Room current;
    private int score;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player p) {
        this.player = p;
    }

    public Room getCurrent() {
        return current;
    }

    public void setCurrent(Room r) {
        this.current = r;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int d) {
        this.score = d;
    }

    public void addScore(int d) {
        this.score += d;
    }

}
