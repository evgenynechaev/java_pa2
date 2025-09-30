package com.example.dungeon.model;

import java.io.Serializable;
import java.util.Objects;

public class Key extends Item implements Serializable {
    private static final String itemClassName = "ключ";

    public Key(String name) {
        super(name);
    }

    /*
    @Override
    public String toString() {
        return String.format("Ключ '%s'",
                super.toString());
    }
    */

    @Override
    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Key key)) {
            return false;
        }

        return Objects.equals(this.getName(), key.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    @Override
    public String getItemClassName() {
        return itemClassName;
    }

    @Override
    public void apply(GameState ctx) {
        System.out.println("Ключ звенит. Возможно, где-то есть дверь...");
    }
}
