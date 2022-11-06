make;
java app/server/Server 0 0 1234 50 25 100 200 5000;
java app/server/Server 0 1 5000 50 25 100 200 1234;
java app/server/ServerManager 230.0.0.0 5000;