@echo off
echo ========================================================
echo   KOMPILASI APLIKASI FUTSAL & MINI SOCCER ARENA MALANG
echo ========================================================

if not exist bin (
    mkdir bin
)

echo Mengompilasi kode program Java...
javac -d bin -sourcepath src src\com\futsalarena\Main.java src\com\futsalarena\test\AppTest.java

if %ERRORLEVEL% EQU 0 (
    echo Kompilasi BERHASIL! Berkas class tersimpan di folder bin/
    echo Membuat berkas JAR yang dapat dieksekusi...
    jar cfe FutsalArenaMalang.jar com.futsalarena.Main -C bin .
    echo Berkas FutsalArenaMalang.jar berhasil dibuat!
) else (
    echo [ERROR] Kompilasi GAGAL! Periksa kode program Anda.
)

pause
