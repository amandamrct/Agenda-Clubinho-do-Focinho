#!/bin/sh
# Compila o código e gera o ClubinhoReservas.jar (precisa do JDK 17+: javac e jar).
cd "$(dirname "$0")"
rm -rf out && mkdir out
javac -encoding UTF-8 -d out $(find src -name '*.java') || exit 1
printf 'Main-Class: clubinho.App\n' > manifesto.txt
jar cfm ClubinhoReservas.jar manifesto.txt -C out . -C resources . || exit 1
rm manifesto.txt
echo "Pronto: ClubinhoReservas.jar"
