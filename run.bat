@echo off
rem chcp 65001
set ROOT=%~dp0
java -cp "%ROOT%out" com.example.dungeon.Main
