all:		Server.class MSocket.class USocket.class ServerManager.class ClockManager.class IClockManager.class

Server.class:	app/server/Server.java
				@javac app/server/Server.java

ServerManager.class:	app/server/ServerManager.java
						@javac app/server/ServerManager.java

MSocket.class:		app/socket/multicast/MSocket.java
					@javac app/socket/multicast/MSocket.java

USocket.class:	app/socket/unicast/USocket.java
				@javac app/socket/unicast/USocket.java

ClockManager.class:	app/server/queue/ClockManager.java
					@javac app/server/queue/ClockManager.java

IClockManager.class:	app/server/queue/IClockManager.java
					@javac app/server/queue/IClockManager.java

clean:
				@rm -rf *.class *~
