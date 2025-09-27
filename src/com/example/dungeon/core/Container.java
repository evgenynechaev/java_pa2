package com.example.dungeon.core;

import com.example.dungeon.model.GameState;
import com.example.dungeon.model.Room;

import java.io.Serializable;
import java.util.List;

public class Container implements Serializable {
    private GameState state;
    private List<Room> rooms;

    public Container(GameState state, List<Room> rooms) {
        this.state = state;
        this.rooms = rooms;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
