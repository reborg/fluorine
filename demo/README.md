### Fluorine Demo

Welcome to the Fluorine Demo Platform, a dockerized environment with 4 Fluorine servers and 8 clients. You can experiment here with config changes and ways the clients reacts to changes.

#### Run

The platform definition is inside `docker-compose.yml`. There are a few scripts to wrap the sequence of docker-compose commands to use to bring up the cluster and tear-down at the end of it.

* `./start-servers.sh` will start all 4 servers at the same time
* `./logs` check that the servers are up and running in the logs
* `./start-client1.sh` will start the first client, again check everything is as expected in the logs
* `./start-client2.sh` starts another client. At this point you should see two pings from the servers to the clients (the keep-alive data) and each server should push 2 configurations down to each client.
* `./start-all-clients.sh` starts up the remaining 6 clients.

#### Docker image

If you change the docker-client, you can rebuild the docker-image for the client with:

* `cd fluorine-client`
* `./docker-rebuild.sh`

#### File synchronization

In a normal non-dockerized environment, file changes events are propagated to the running JVM just fine. But docker containers are not propagating file system events of mounted file systems (the case with the demo platform) up to the running processes.

As a workaround, you need to install a small utility called docker-osx-dev that takes care of synching files between the virtualized environments through rsync. It is available here: https://github.com/brikis98/docker-osx-dev. When installed do the following:

* `cd demo/fluorinedemo`
* `./sync`

This will start the rsync process so when you change configuration files they will be actually pushed to all clients. **NB:** again, this is just needed for the dockerized environment. It works perfect on bare-metal or non-dockerized virtual environments.

#### Troubleshooting

* If no configuration is sent, make sure all files in the configuration folder have full read-write permissions for all users. Just in case: `cd demo; chmod -R 777 fluorinedemo;` Again this is only in dockerized environments.
