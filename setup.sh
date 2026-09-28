#!/usr/bin/env bash
set -euo pipefail

FILENAME="$1"
SLEEP=15

COUNTER=$(wc -l < "$FILENAME")
LOGNAME=$(basename "${FILENAME%.txt}")

echo "CREATING PROCESSES..."

for ((i = 0; i < COUNTER; i++)); do
	gnome-terminal -e "bash -c \"java app/server/ServerSetup ${FILENAME} ${i} 2>&1 | tee logs/${LOGNAME}_${i}.txt ;exec bash; \""
done

echo "PROCESSES CREATED."

echo "SLEEPING..."
sleep "$SLEEP"
echo "AWAKE."

echo "UNLOCKING ALL SERVERS."

gnome-terminal -e "bash -c \"java app/server/ServerManager 230.0.0.0 5000;exec bash; \""
