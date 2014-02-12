#!/bin/bash

FILENAME=session${SIZE}.bson
echo "Using session sample: $FILENAME"

#ab -c 10 -n 1000 -p $FILENAME -T 'application/multipart-form-data' http://localhost:8080/topics/messages
#ab -v 3 -c 10 -n 1000 -p $FILENAME -T 'application/bson' http://localhost:8080/topics/messages
ab -v 0 -c 100 -n 1000 -p $FILENAME -T 'application/bson' http://localhost:8080/topics/messages
