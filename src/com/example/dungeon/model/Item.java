package com.example.dungeon.model;

import java.io.Serializable;

public abstract class Item implements Serializable {
    private final String name;

    protected Item(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public abstract String getItemClassName();

    public abstract void apply(GameState ctx);
}
