package com.example.dungeon.core;

import com.example.dungeon.model.GameState;
import com.example.dungeon.model.Room;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Container implements Serializable {
    private String currentRoom;
    private GameState state;
    private Map<String, Room> rooms;
    // private List<Room> rooms;

    public Container(GameState state, Map<String, Room> rooms, String currentRoom) {
        this.currentRoom = currentRoom;
        this.state = state;
        this.rooms = rooms;
    }

    /*
    @Override
    public String toString() {
        return String.format("Контейнер\ncurrentRoom: '%s'\nstate: %s\nrooms: %s\n",
                this.currentRoom,
                this.state,
                this.rooms);
    }
    */

    public String getCurrentRoom() {
        return this.currentRoom;
    }

    public void setCurrentRoom() {
        this.currentRoom = this.state.getCurrent().getName();
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public void setRooms(Map<String, Room> rooms) {
        this.rooms = rooms;
    }
}
