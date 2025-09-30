package com.example.dungeon.model;

import com.example.dungeon.core.EndGameException;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Player extends Entity implements Serializable {
    private int attack;
    private final List<Item> inventory = new ArrayList<>();

    public Player(String name, int hp, int attack) {
        super(name, hp);
        this.attack = attack;
    }

    /*
    @Override
    public String toString() {
        return String.format("Player '%s', атака: %d",
                super.toString(),
                this.attack);
    }
    */

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public void setInventory(Item item) {
        if(item == null) return;
        this.inventory.add(item);
        System.out.println("Взято: " + item);
    }

    public void setInventoryList(List<Item> items) {
        if(items == null) return;
        this.inventory.addAll(items);
        System.out.println("Взято:");
        items.forEach(System.out::println);
    }

    @Override
    public String describe() {
        return "Игрок: " + this.getName() +
                "\nТекущее HP: " +
                this.getHp() +
                "\nАтака: " +
                this.getAttack() +
                "\n" +
                this.inventoryText();
    }

    public String inventoryText() {
        StringBuilder sb = new StringBuilder("Инвентарь:");
        Map<String, List<Item>> map = this.getInventory()
                .stream()
                .sorted(Comparator.comparing(Item::getItemClassName).thenComparing(Item::getName))
                .collect(Collectors.groupingBy(Item::getItemClassName));
        if(map.isEmpty()) {
            sb.append("\nпусто");
        } else {
            map.forEach((key, value) -> {
                String items = String.join(", ", value.stream().map(Item::getName).toList());
                sb.append(String.format("\n%s: %s", key, items));
            });
        }
        return sb.toString();
    }

    @Override
    public void takeHit(int power) {
        super.takeHit(power);
        System.out.printf("Монстр бьете вас на %d. Ваше HP: %d\n", power, this.getHp());
        if(this.getHp() <= 0) {
            throw new EndGameException("\nИгрок погиб! Игра завершена. Нажмите [Ввод]");
        }
    }
}
