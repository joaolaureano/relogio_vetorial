import os
from time import sleep
import sys

FILENAME = sys.argv[1]
SLEEP = 15

file = open(FILENAME)

counter = len(file.readlines())

print("CREATING PROCESSES...")
for i in range(counter):
    operation = "gnome-terminal -e 'bash -c \"java app/server/ServerSetup {filename} {value};exec bash; \"\' ".format(
        filename=FILENAME, value=i)
    # print(operation)
    os.system(operation)
print("PROCESSES CREATED.")

print("SLEEPING...")
sleep(SLEEP)
print("AWAKE.")

print("UNLOCKING ALL SERVERS.")

operation = "gnome-terminal -e 'bash -c \"java app/server/ServerManager 230.0.0.0 5000;exec bash; \"\' "
os.system(operation)
