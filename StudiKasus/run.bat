@echo off
echo ========================================================
echo   MENJALANKAN FUTSAL & MINI SOCCER ARENA MALANG
echo ========================================================

if not exist bin\com\futsalarena\Main.class (
    echo Mengompilasi program terlebih dahulu...
    if not exist bin mkdir bin
    javac -d bin -sourcepath src src\com\futsalarena\Main.java
)

start javaw -cp bin com.futsalarena.Main
echo Aplikasi berhasil dijalankan!
