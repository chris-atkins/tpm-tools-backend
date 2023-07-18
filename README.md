Some useful commands:

Run all tests:
```gradle test --rerun-tasks --no-build-cache```

Set up a local backend - creates a new DB docker container, builds a docker image from this project, and starts a new container from the image, connected to the DB container.  This is especially useful for local testing of the frontend - cypress tests or just manual tests.
```./start-local-server-with-db.sh```

