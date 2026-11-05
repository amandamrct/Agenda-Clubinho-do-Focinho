#!/bin/sh
# Abre o sistema de reservas (precisa do Java 17 ou mais novo).
cd "$(dirname "$0")"
java -jar ClubinhoReservas.jar "$@"
