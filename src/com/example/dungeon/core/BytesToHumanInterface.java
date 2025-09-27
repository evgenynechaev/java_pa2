package com.example.dungeon.core;

@FunctionalInterface
public interface BytesToHumanInterface<String> {
    String convert(long bytes);
}
