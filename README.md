![Fluorine Protons](https://dl.dropboxusercontent.com/u/1740372/fluorine.png)

[![Clojars Project](https://img.shields.io/clojars/v/net.reborg/fluorine.svg)](https://clojars.org/net.reborg/fluorine)

## Why Fluorine?

Because configuration is extremely "reactive": all applications form "compounds" with some config library. But Fluorine is more than just a library, it is a **client/server model for distributed configuration**. Fluorine enables the following scenario:

* You have a Clojure system running on 2+ nodes. Nodes might be running the same application or different services, hosted in-house or some cloud service.
* Applications are stored in separate git repos (svn, hg, or anything else), so you can develop and deploy them independently.
* Configurable parts of the applications are stored on plain edn files, including startup configuration (stuff needed while bootstrapping like ports, locations, etc) or runtime configuration (for example feature toggles).
* But configuration is stored in another, centralized, repository. All services edn files will live under the same root folder, that you can structure with any sub-level of subfolders/files.
* When services start, they connect to Fluorine server to receive their configuration and bootstrap data.
* Whenever a configuration file is changed in the centralized repository, the running applications interested in the file will receive the updated configuration.

Fluorine-server will sit somewhere with a local pull of the configuration repository and watch for changes. Fluorine-client (at the moment Clojure only) will connect to the server and register for change events for some portion of the configuration tree.

### What Fluorine offers?

The main selling point of Fluorine is to suggest a complete configuration model, not just a library to read properties. Other important facts include:

* Plain text instead of proprietary serialization formats (only edn now, but json and custom formats planned). Plain text is readable and can be easily put under source control, where changes can be tracked easily and "diffing" is well supported by tools.
* Client/Server communication via web-sockets is firewall friendly and cross-language enabling other languages than Clojure to use Flurine (as soon as other clients are ready).
* Resiliency and fail-over capabilities: you can run many Fluorine Server instances and clients know how to fail-over gracefully. Even if all Fluorine servers are unavailable, a local cache will keep your system running.

### How Fluorine compares to other solutions?

* Zookeeper: offers config-hosting (along with other big features like distributed synchronization). Config in Zookeeper is stored in some binary format that you are supposed to access with a client or via APIs. It's a very robust solution but you can't access configuration as plain text (you can of course store as much plain-text you like, it's just not stored as such on the file system). Zookeeper also has specific operational costs: you need to know how to configure and backup a cluster. Fluorine is way easier to operate and understand being focused on one main task only (no distributed semaphores, just config). Fluorine doesn't use any proprietary format and read configuration as plain files from any file system.
* Etcd: also offers configuration hosting. It's more user-friendly than Zookeeper and easier to operate. It still stores configuration in a proprietary format and requires you to use the APIs to change or access the configuration. https://github.com/bradgignac/slingshot is an attempt to create a tool based on the file system to synchronize changes with Etcd. But now you have to install two things instead of one. Fluorine offers similar capabilities in a single package.

## How to install

*server*

Check the current version at the top of the page if the one below is not the same.

```bash
curl -O https://github.com/reborg/fluorine/releases/download/0.0.3/fluorine.jar
echo '{:fluorine-root "/path/to/data/folder"}' > fluorine.config
java -jar fluorine.jar
```

Notice that `:fluorine-root` should point to the folder that Fluorine should watch for serving/changing configuration files.

*client*

Check the current version at the top of the page if the one below is not the same. Probably better to use the most recent.

```clojure
:dependencies [[net.reborg/fluorine "0.0.3"]]
```

## How to use

Here's a typical REPL session:

```clojure
user=> (require '[net.reborg.fluorine-client :as c])

; connects to the two servers asynchronously, assuming there is a folder /apps/myapp1
; under <fluorine-root>. The connection happens through a keep-alive HTTP
; WebSocket connection by default.

user=> (c/attach "/apps/myapp1/" "fluorine-host1,fluorine-host2" 10101)

;; your configuration is now available by dereferencing the following atom:

user=> @c/cfg
{:myapp1 {:prop1 "hello" :prop2 "world"}}

;; Following read of the atom might change based on configuration changes.
```

Both Fluorine servers in this example will print the same information when a client connects:

    - reaction request from 127.0.0.1 for path /apps/myapp1/

Now try to change a file in the watched folder in one of the servers and you should see something like this on the client:

    - received new config [{:prop2 "world"} {:prop2 "fluorine"} {:myapp1 {:prop1 "hello" :prop2 "fluorine"}}]

Which shows an array with 3 elements: the old value, the new value that changed, the entire configuration map. At the same time you should see the following on the server:

    - file /me/prj/my/fluorine/data/apps/myapp1/ changed. firing watcher for [127.0.0.1 /apps/myapp1/]

The new config that was received from the client is the content of the file that was changed in edn format. By the time it arrives on the client it is already a proper Clojure data structure ready to use.

## Running with multiple Fluorine Servers

Apart from using a comma separated list of all the servers you want to connect from the client, the only other thing to take in mind is that now all Fluorine Servers will compete against all clients to push a new configuration. So if you run with multiple Fluorine Servers you should always make sure that changes to the configuration repository are always synchronized and executed simultaneously (or in a very close sequence). The principle is that the last Fluorine Server to realize there is a change to push will overwrite all the others on all client (which is not a problem).

## todo

* [ ] json format support
* [ ] other custom pluggable formats
* [ ] JavaScript client
* [ ] tcp, udp?
* [ ] stress testing (how many connected clients? bandwidth? changes?)
* [ ] homebrew tap for quick install on mac
* [ ] apt-get ubuntu package?
