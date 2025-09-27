package com.example.dungeon.model;

import java.io.Serializable;
import java.util.*;

public class Room implements Serializable {
    private final String name;
    private final String description;
    private Key locked = null;
    private final Map<String, Room> neighbors = new HashMap<>();
    private final List<Item> items = new ArrayList<>();
    private Monster monster;

    public Room(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Room(String name, String description, Key locked) {
        this.name = name;
        this.description = description;
        this.locked = locked;
    }

    public String getName() {
        return name;
    }

    public Map<String, Room> getNeighbors() {
        return neighbors;
    }

    public Key getLocked() {
        return locked;
    }

    public void setLocked (Key locked) {
        this.locked = locked;
    }

    public List<Item> getItems() {
        return items;
    }

    public Monster getMonster() {
        return monster;
    }

    public void setMonster(Monster m) {
        this.monster = m;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder(name + ": " + description);
        if (!items.isEmpty()) {
            sb.append("\nПредметы: ")
                    .append(String.join(", ", items.stream().map(Item::toString).toList()));
        }
        if (monster != null) {
            sb.append("\nВ комнате монстр: ")
                    .append(monster.getName())
                    .append(" (ур. ")
                    .append(monster.getLevel())
                    .append(", HP: ")
                    .append(monster.getHp()).append(")")
                    .append(monster.getHp() <= 0 ? " Побежден!" : "");
        }
        if (!neighbors.isEmpty()) {
            sb.append("\nВыходы: ")
                    .append(String.join(", ", neighbors.keySet()));
        }
        return sb.toString();
    }
}
