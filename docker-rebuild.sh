lein clean
lein uberjar
docker build -t reborg/fluorine .
lein clean
docker stop `docker ps -a | grep fluo | awk '{print $1}'`
docker rm `docker ps -a | grep fluo | awk '{print $1}'`
