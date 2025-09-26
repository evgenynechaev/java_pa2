package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;

public class Game {
    private final String version = "v.1.0";
    private final GameState state = new GameState();
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private List<Room> rooms = new ArrayList<>();

    static {
        WorldInfo.touch("Game");
    }

    public Game() {
        registerCommands();
        bootstrapWorld();
    }

    private void registerCommands() {
        commands.put("about", (ctx, a)
                -> System.out.println("Игра Dungeon Mini " + version));
        commands.put("help", (ctx, a)
                -> System.out.println("Команды: " + String.join(", ", commands.keySet())));
        commands.put("gc-stats", (ctx, a) -> {
            System.out.println(this.gcStats());
        });
        commands.put("look", (ctx, a)
                -> System.out.println(ctx.getCurrent().describe()));
        commands.put("self", (ctx, a)
                -> System.out.println(ctx.getPlayer().describe()));
        commands.put("move", (ctx, a) -> {
            if(a == null || a.isEmpty()) {
                throw new InvalidCommandException("Не указано куда двигаться");
            }
            Optional<Room> optNewRoom = Optional.ofNullable(ctx.getCurrent().getNeighbors().get(a.getFirst()));
            if(optNewRoom.isEmpty()) {
                throw new InvalidCommandException("Туда нет пути");
            }

            Room newRoom = optNewRoom.get();
            Optional<Key> key = Optional.ofNullable(newRoom.getLocked());
            if(key.isPresent()) {
                if(state.getPlayer().getInventory().stream()
                        .filter(x -> x.getClass()
                                .getSimpleName()
                                .equals(Key.class.getSimpleName()))
                        .noneMatch(x -> x.equals(key.get()))) {
                    throw new InvalidCommandException(
                            "Нет ключа от локации '"
                                    + newRoom.getName()
                                    + "'. Найдите и возьмите ключ");
                }
            }

            state.setCurrent(newRoom);
            System.out.println("Вы перешли в: " + newRoom.getName());
            commands.get("look").execute(state, null);
        });
        commands.put("take", (ctx, a) -> {
            if(a == null || a.isEmpty()) {
                throw new InvalidCommandException("Не указано что брать");
            }
            Optional<Item> item = ctx.getCurrent().getItems().stream()
                    .filter(x -> x.getName().equalsIgnoreCase(a.getFirst())).findFirst();
            if(item.isPresent()) {
                state.getPlayer().setInventory(item.get());
                ctx.getCurrent().getItems().remove(item.get());
            }
            else {
                throw new InvalidCommandException("Нет такого предмета");
            }
        });
        commands.put("inventory", (ctx, a)
                -> System.out.println(state.getPlayer().inventoryText()));
        commands.put("use", (ctx, a) -> {
            if(a == null || a.isEmpty()) {
                throw new InvalidCommandException("Не указано что использовать");
            }
            Optional<Item> item = ctx.getPlayer().getInventory().stream()
                    .filter(x -> x.getName().equalsIgnoreCase(a.getFirst())).findFirst();
            if(item.isPresent()) {
                item.get().apply(ctx);
            }
            else {
                throw new InvalidCommandException("Нет такого предмета");
            }
        });
        commands.put("fight", (ctx, a) -> {
            Optional<Monster> optMonster = Optional.ofNullable(ctx.getCurrent().getMonster());
            if(optMonster.isEmpty()) {
                throw new InvalidCommandException("В этом месте нет монстра");
            }

            System.out.println("Битва");
            Player player = ctx.getPlayer();
            Monster monster = optMonster.get();

            if(monster.getHp() <= 0) {
                throw new InvalidCommandException("Монстр уже побежден!");
            }

            monster.takeHit(player.getAttack());
            player.takeHit(monster.getLevel());

            if(monster.getHp() <= 0) {
                System.out.println("Монстр побежден!");
                Optional<List<Item>> item = Optional.ofNullable(monster.getLoot());
                player.setInventoryList(item.orElse(null));
                monster.setLoot(null);
            }
        });
        commands.put("save", (ctx, a)
                -> SaveLoad.save(ctx));
        commands.put("load", (ctx, a)
                -> SaveLoad.load(ctx, this.rooms));
        commands.put("scores", (ctx, a)
                -> SaveLoad.printScores());
        commands.put("exit", (ctx, a)
                -> this.exit());
        commands.put("quit", (ctx, a)
                -> this.exit());
    }

    private void exit() {
        System.out.println("Пока!");
        System.exit(0);
    }

    private void bootstrapWorld() {
        Player hero = new Player("Герой", 20, 5);
        // hero.setInventory(new Key("Ключ-серебряный"));
        // hero.setInventory(new Key("Ключ-Лес"));
        // hero.setInventory(new Weapon("Меч-кладенец", 20));
        // hero.setInventory(new Potion("Большое-зелье", 20));
        // hero.setInventory(new Potion("Малое-зелье", 5));
        state.setPlayer(hero);

        Room square = new Room("Площадь", "Каменная площадь с фонтаном.");
        Room forest = new Room("Лес", "Шелест листвы и птичий щебет.", new Key("Ключ-Лес"));
        Room cave = new Room("Пещера", "Темно и сыро.", new Key("Ключ-Пещера"));
        Room castle = new Room("Замок", "Королевский дворец.", new Key("Ключ-Замок"));
        this.rooms = new ArrayList<>(List.of(square, forest, cave, castle));

        square.getNeighbors().put("north", forest);
        square.getNeighbors().put("west", castle);
        forest.getNeighbors().put("south", square);
        forest.getNeighbors().put("east", cave);
        cave.getNeighbors().put("west", forest);
        castle.getNeighbors().put("east", square);

        square.getItems().add(new Key("Ключ-Лес"));
        square.getItems().add(new Weapon("Меч", 15));
        square.getItems().add(new Potion("Большое-зелье", 15));
        forest.getItems().add(new Potion("Малое-зелье", 5));
        forest.setMonster(new Monster("Волк", 10, 8,
                new ArrayList<>(
                        List.of(new Key("Ключ-Пещера"),
                                new Potion("Среднее-зелье", 10),
                                new Weapon("Шпага", 10))
                )
        ));
        cave.getItems().add(new Key("Ключ-Замок"));

        state.setCurrent(square);
    }

    public void run() {
        System.out.println("DungeonMini (TEMPLATE). 'help' - команды.");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "cp866"))) {
            while (true) {
                System.out.print("> ");
                String line = in.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> parts = Arrays.asList(line.split("\\s+"));
                String cmd = parts.getFirst().toLowerCase(Locale.ROOT);
                List<String> args = parts.subList(1, parts.size());
                Command c = commands.get(cmd);
                try {
                    if (c == null) throw new InvalidCommandException("Неизвестная команда: " + cmd);
                    c.execute(state, args);
                    state.addScore(1);
                } catch (InvalidCommandException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (EndGameException e) {
                    System.out.println(e.getMessage());
                    this.exit();
                } catch (Exception e) {
                    System.out.println("Непредвиденная ошибка: "
                            + e.getClass().getSimpleName()
                            + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода: " + e.getMessage());
        }
    }

    private String gcStats() {
        Runtime rt = Runtime.getRuntime();
        long beforeFree = rt.freeMemory();
        long beforeTotal = rt.totalMemory();
        StringBuilder sb = new StringBuilder();
        sb.append("   До GC. Память: использованная=")
                .append(this.humanReadableByteCountBin(beforeTotal - beforeFree))
                .append(" свободная=")
                .append(this.humanReadableByteCountBin(beforeFree))
                .append(" общая=")
                .append(this.humanReadableByteCountBin(beforeTotal));
        System.gc();
        long afterFree = rt.freeMemory();
        long afterTotal = rt.totalMemory();
        sb.append("\nПосле GC. Память: использованная=")
                .append(this.humanReadableByteCountBin(afterTotal - afterFree))
                .append(" свободная=")
                .append(this.humanReadableByteCountBin(afterFree))
                .append(" общая=")
                .append(this.humanReadableByteCountBin(afterTotal));
        return sb.toString();
    }

    private String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }
}
