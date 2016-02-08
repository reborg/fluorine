![Fluorine Protons](https://dl.dropboxusercontent.com/u/1740372/fluorine.png)

## Why Fluorine?

Because configuration is extremely reactive: all applications form compounds with some config management library. Now seriously, imagine the following recipe:

* You have some large number of nodes running your application. You've been careful putting things in a configuration files since the beginning including a great deal of feature toggles (http://martinfowler.com/bliki/FeatureToggle.html) so you can switch things on and off easily.
* You also designed your app so configuration is centralized in a namespace/class/module (depending on the language stack). If configuration changes, your app will just read the new values.
* Your configuration files are sitting somewhere in a git repo, organized by folders corresponding to all the different apps.

The fluorine-server will sit somewhere close to the git repo containing the configuration and will watch for changes to the files sitting there. The fluorine-client (at the moment Clojure only) will be connected to the server and registered for change events for some portion of the configuration tree. Now, everytime a pull to the git repo contains changes running applications will be notified, bounce themselves and receives a brand new config. No need to restart services or worse, re-deploy apps.

The main selling points of Fluorine are:

* Plain text configuration files (only EDN now, json and custom formats will also be supported soon), no proprietary formats or databases, so it can be easily put under source control
* Multiple clients for different languages can listen for changes using WebSockets (only the Clojure client is ready now)
* Smart caching, resiliency and failover strategies when the server is down and unable to deliver configuration changes

How Fluorine compares to other tools?

* Zookeeper: offers config-hosting (along with other big features like distributed synchronization). Config in Zookeeper is stored in some binary format that you are supposed to access with a client or via APIs. It's a very robust solution but you can't store configuration as plain text unless you build your tooling around it. Zookeeper also requires specific operational costs: you need to know how to configure a cluster and some of the concepts around it. Check the getting started guide to learn more. Fluorine is way more easier to operate and understand (being focused on one main task only). Fluorine doesn't use any proprietary format requirements and stores your configuration on the file system.
* Etcd: also offers config-hosting. Is more user-friendly than Zookeeper and easier to operate. It still stores configuration in a proprietary format and requires you to use the APIs to change or access the configuration. https://github.com/bradgignac/slingshot is an attempt to create a tool based on the file system to synchronize changes with Etcd. But now you have to install two things instead of one. Fluorine offers similar capabilities in a single package.

_Current status_: Fluorine is in ready to use state but it has not been battle-tested nor stress-tested. So expect some developing, fixes and big changes in the next future. The main areas to make it production ready are server failover/clustering and smarter clients (with caching and failover capabilities). The reason it's public already is in the hope somebody wants to help me out.

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

The new config that was received from the client is the content of the file that was changed in edn format. By the time it arrives on the client it is already a proper Clojure data structure ready to use.

## todo

* [ ] json format support
* [ ] other custom pluggable formats
* [ ] JavaScript client
* [ ] tcp, udp
* [ ] client side config caching
* [ ] client side reconnection logic (if server unavailable just use last received config)
* [ ] server side clustering (for fail-over, the client could try to connect to another server if one goes down)
* [ ] stress testing (how many connected clients? bandwidth? changes?)
* [ ] homebrew tap for quick install on mac
* [ ] apt-get ubuntu package?
