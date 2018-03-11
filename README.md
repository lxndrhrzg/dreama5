## dreama 5 (yes, I actually recoded it almost from scratch five times)

* remote administration tool for simpler handling of multiple servers running automated tasks and communicating with each other (focussed on botting MMO games)

* architecture:
	* tomcat webserver for web-interface as centralised control panel (dreama5/hive/servlets). Servlets act as short-living clients (clients described later). 
	* tomcat servlets use critical frontend-javascript to interact with backend REST-api. (ROOT_copy/assets/js) assets folder is quite messy because based on mobirise and never refactored.
	* single central server to process and forward client-communication through tcp sockets (netty lib). (I call this server "hive") (ran with dreama5/hive/sockets/DreamServer.java)
	* multiple clients connecting to hive only. listening to his commands or sending him commands (also through tcp sockets, obviously). (I call these clients "dreamlings") (ran with dreama5/dreamling/dreamling/client/Dreamling.java)

* finished this project in a matter of weeks. refactoring for years now. touch it with carefulness please, it's my baby

### Todo list for myself

* hive saves all dreamlings ever connected to. make hive expire and delete old ones?
* synchronize collection-iterations (dreamserver and others). 
* run persistant saving of dreamlings after each change on hive
* double-check account synchronisation between dreamling and hive
* stresstest