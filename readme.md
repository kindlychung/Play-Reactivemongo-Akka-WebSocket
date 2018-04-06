# Demo of play-reactivemongo with a websocket endpoint

* `sudo service mongod start`
* In mongo client: 
```
use playwebsocketdemo; 
db.createCollection("users", { capped: true, size: 3 });
``````

You can make the size bigger, of course, or any customization you want.


<!-- tags: scala, play, web, framework, mongo, mongodb -->
