# quarkus-app-revanche

Init database
```
docker compose -f ../docker-compose-banco.yaml up
```

Build Quarkus

```
./build.sh
```

Init Quarkus

```
java -jar -Ddb_host=localhost -Dquarkus.log.console.enable=true target/quarkus-app/quarkus-run.jar
```

Run Test

```
stress-test/run-test.sh
```

My Last report (Macos Intel)

stress-test/last-report/rinhabackendsimulation-20230904231641603/index.html