all:				Server.class MSocket.class USocket.class ServerSender.class

Server.class:		app/server/Server.java
				@javac app/server/Server.java

ServerSender.class:		app/server/ServerSender.java
				@javac app/server/ServerSender.java

MSocket.class:		app/socket/multicast/MSocket.java
				@javac app/socket/multicast/MSocket.java

USocket.class:		app/socket/unicast/USocket.java
				@javac app/socket/unicast/USocket.java

clean:
				@rm -rf *.class *~
