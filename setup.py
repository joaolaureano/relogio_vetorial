import os
from time import sleep
import sys

FILENAME = sys.argv[1]
SLEEP = 15

file = open(FILENAME)

counter = len(file.readlines())

print("CREATING PROCESSES...")

for i in range(counter):
    logfilename = "{logname}_{id}.txt".format(
        logname=os.path.basename(FILENAME.split(".txt")[0]), id=i)
    operation = "gnome-terminal -e 'bash -c \"java app/server/ServerSetup {filename} {value} 2>&1 | tee logs/{logname} ;exec bash; \"\' ".format(
        filename=FILENAME, value=i, logname=logfilename)
    # print(operation)
    os.system(operation)
print("PROCESSES CREATED.")

print("SLEEPING...")
sleep(SLEEP)
print("AWAKE.")

print("UNLOCKING ALL SERVERS.")

operation = "gnome-terminal -e 'bash -c \"java app/server/ServerManager 230.0.0.0 5000;exec bash; \"\' "
# print(operation)
os.system(operation)
