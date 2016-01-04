## Why Fluorine?

Because configuration is extremely reactive: all applications form compounds with some config management library.

Now seriously. Fluorine is distributed configuration the Clojure way:

* edn as main configuration format (json also supported)
* file based, so it can be easily put under source control
* notifies all connected clients for changes to the configuration subset they are interested in (thanks to aleph, fluorine speaks WebSockets, tcp or udp).
* provides an easy to use fluorine-client to talk to the server

## Current status

Fluorine is in ready to use state but it has not been battle-tested nor stress-tested. So expect some developing, fixes and big changes in the next future.

## How to install

*server*

At the moment installation is straight from sources. A more formal approach will follow (brew/apt-get):

```bash
git clone https://github.com/reborg/fluorine
lein uberjar
echo '{:fluorine-root "/path/to/data/folder"}' > fluorine.config
java -jar target/fluorine.jar
```

Notice that `:fluorine-root` should point to the folder that Fluorine should watch for serving/changing configuration files.

*client*

```clojure
:dependencies [[net.reborg/fluorine "0.0.1"]]
```

## How to use

Here's a typical REPL session:

```clojure
(require '[net.reborg.fluorine-client :as c])

; connects to the running server and register for changes to the configuration
; path "/apps/clj-fe". The anonymuous callback function is just going to print
; received events on screen. The connection happens through a keep-alive HTTP
; WebSocket connection by default.

(c/attach "/apps/clj-fe" (fn [cfg] (println "received new config" cfg)))
```

The running server will print information when a client connects:

    08:59:06.971 WARN net.reborg.fluorine - reaction request from 127.0.0.1 for path /apps/clj-fe

Now try to change a file in the watched folder and you should see something like this on the client:

    received new config {:clj-fe {:someprop false}}

and the following on the server:

    09:00:19.017 WARN net.reborg.fluorine - file /me/prj/my/fluorine/data/apps/clj-fe/someprop changed. firing watcher for 127.0.0.1

## todo

* [ ] json format support
* [ ] pluggable formats
* [ ] tcp support
* [ ] failover and reliability
* [ ] stress testing
* [ ] homebrew tap for quick install on mac
* [ ] how to create an ubuntu package?
