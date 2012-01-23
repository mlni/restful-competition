RESTful Competition
===================

What is this?
-------------

This is an application for running an online programming competition with your buddies/company/user group. The structure and spirit of the project is heavily inspired by a similar project called Extreme Startup (https://github.com/rchatley/extreme_startup). 


Why?
----

Because I happened to take part in an Extreme Startup event and found it immensely fun. And I didn't know that the server running the competition was open source. So I wrote my own, because playing with asynchronous http seemed like a good idea, and I got a bunch of ideas for the competition exercises.


How does it work?
-----------------

The competition starts by a facilitator running the server (lein run) and announcing the address of the server.
The players are not told anything about the nature of the questions not about the scoring algorithms. The players register
their own http server with the game and start receiving questions as http requests, as well as being scored for their answers.
From there on it's up to the players to figure out clever ways to beat competitors with ways to find faster answers, avoid getting
punished and get higher scores.

The game server itself is written in Clojure. The technology that players can use is not limited in any way: if your platform can speak HTTP then it will work. See https://github.com/bodil/extreme_startup_servers for an inspiration for servers to get you started.


Who?
----

Matti Jagula <matti.jagula@gmail.com>

