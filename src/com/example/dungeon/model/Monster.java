package com.example.dungeon.model;

import com.example.dungeon.core.InvalidCommandException;

import java.io.Serializable;
import java.util.List;

public class Monster extends Entity implements Serializable {
    private int level;
    private List<Item> loot;

    public Monster(String name, int level, int hp) {
        super(name, hp);
        this.level = level;
    }

    public Monster(String name, int level, int hp, List<Item> loot) {
        super(name, hp);
        this.level = level;
        this.loot = loot;
    }

    /*
    @Override
    public String toString() {
        return String.format("Monster '%s', уровень: %d",
                super.toString(),
                this.level);
    }
    */

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Item> getLoot() {
        return loot;
    }

    public void setLoot(List<Item> loot) {
        this.loot = loot;
    }

    @Override
    public String describe() {
        return "Монстр: " + this.getName() + "\nТекущее HP: " + this.getHp() + "\nАтака: " + this.getLevel();
    }

    @Override
    public void takeHit(int power) {
        if(this.getHp() <= 0) {
            throw new InvalidCommandException("Монстр уже повержен!");
        }
        super.takeHit(power);
        System.out.printf("Вы бьете %s на %d. HP монстра: %d\n", this.getName(), power, this.getHp());
    }
}
