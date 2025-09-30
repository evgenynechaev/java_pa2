package com.example.dungeon.core;

import com.example.dungeon.model.*;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Game {
    private final String version = "v.1.0";
    private final GameState state = new GameState();
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Map<String, Room> rooms = new HashMap<>();

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
        commands.put("gc-stats", (ctx, a) -> System.out.println(this.gcStats()));
        commands.put("look", (ctx, a)
                -> System.out.println(ctx.getCurrent().describe()));
        commands.put("self", (ctx, a)
                -> System.out.println(ctx.getPlayer().describe()));
        commands.put("move", this::move);
        commands.put("take", this::take);
        commands.put("inventory", (ctx, a)
                -> System.out.println(state.getPlayer().inventoryText()));
        commands.put("use", this::use);
        commands.put("fight", this::fight);
        // commands.put("save", (ctx, a)
        //         -> SaveLoad.save(ctx));
        commands.put("save", (ctx, a)
                -> SaveLoad.saveSerializable(new Container(ctx, this.rooms, ctx.getCurrent().getName())));
        // commands.put("load", (ctx, a)
        //         -> SaveLoad.load(ctx, this.rooms));
        commands.put("load", (ctx, a)
                -> SaveLoad.loadSerializable(new Container(ctx, this.rooms, ctx.getCurrent().getName())));
        commands.put("scores", (ctx, a)
                -> SaveLoad.printScores());
        commands.put("exit", (ctx, a)
                -> this.exit());
        commands.put("quit", (ctx, a)
                -> this.exit());
    }

    private void move(GameState ctx, List<String> a) {
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
    }

    private void take(GameState ctx, List<String> a) {
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
    }

    private void use(GameState ctx, List<String> a) {
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
    }

    private void fight(GameState ctx, List<String> a) {
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
    }

    private void exit() {
        System.out.println("Пока!");
        System.exit(0);
    }

    private void bootstrapWorld() {
        String heroName = "Герой";
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.printf("Введите имя игрока (%s): ", heroName);
            String line = scanner.nextLine();
            if (line == null) continue;
            String name = line.trim();
            if (name.isEmpty()) {
                break;
            }
            heroName = name;
            break;
        }
        Player hero = new Player(heroName, 30, 5);
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

        this.rooms.put(square.getName(), square);
        this.rooms.put(forest.getName(), forest);
        this.rooms.put(cave.getName(), cave);
        this.rooms.put(castle.getName(), castle);

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
                    in.readLine();
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
        Utilities util = new Utilities();
        Runtime rt = Runtime.getRuntime();
        long beforeFree = rt.freeMemory();
        long beforeTotal = rt.totalMemory();
        StringBuilder sb = new StringBuilder();
        sb.append("   До GC. Память: использованная=")
                .append(util.bytesToHuman.convert(beforeTotal - beforeFree))
                .append(" свободная=")
                .append(util.bytesToHuman.convert(beforeFree))
                .append(" общая=")
                .append(util.bytesToHuman.convert(beforeTotal));
        System.gc();
        long afterFree = rt.freeMemory();
        long afterTotal = rt.totalMemory();
        sb.append("\nПосле GC. Память: использованная=")
                .append(util.bytesToHuman.convert(afterTotal - afterFree))
                .append(" свободная=")
                .append(util.bytesToHuman.convert(afterFree))
                .append(" общая=")
                .append(util.bytesToHuman.convert(afterTotal));
        return sb.toString();
    }
}
