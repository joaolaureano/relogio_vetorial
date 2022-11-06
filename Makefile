all:		Server.class MSocket.class USocket.class ServerManager.class ClockManager.class IClockManager.class Sleeper.class EventManager.class ServerListener.class ServerSetup.class

Server.class:	app/server/Server.java
				@javac app/server/Server.java

ServerManager.class:	app/server/ServerManager.java
						@javac app/server/ServerManager.java

MSocket.class:		app/socket/multicast/MSocket.java
					@javac app/socket/multicast/MSocket.java

USocket.class:	app/socket/unicast/USocket.java
				@javac app/socket/unicast/USocket.java

ClockManager.class:	app/server/clock/ClockManager.java
					@javac app/server/clock/ClockManager.java

IClockManager.class:	app/server/clock/IClockManager.java
						@javac app/server/clock/IClockManager.java

Sleeper.class:	app/server/sleeper/Sleeper.java
				@javac app/server/sleeper/Sleeper.java

EventManager.class:	app/server/event/EventManager.java
					@javac app/server/event/EventManager.java

ServerListener.class:	app/server/ServerListener.java
						@javac app/server/ServerListener.java

ServerSetup.class:		app/server/ServerSetup.java
						@javac app/server/ServerSetup.java
clean:
				@rm -rf *.class *~
