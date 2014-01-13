Kafka HTTP endpoint
-------------------

Rationale
---------
Kafka high level Producer and Consumer APIs are very hard to implement right.
Rest endpoint gives access to native Scala high level consumer and producer APIs.


Producer Endpoint API
----------------------

Producer endpoint accepts messages in batches in json or bson formats to the topic of choice.

```bash
curl -X POST -H "Content-Type: application/json"\
             -d '{"messages": [{"key": "key", "value":{"val1":"hello"}}]}\
              http://localhost:8090/topics/messages
```

Endpoint can be configured to be sync or asyncronous.


Consumer Endpoint API
----------------------

Consumer endpoint uses long-polling to consume messages in batches in json or bson formats:

Example request:

```bash
curl -H "Accept:application/json" -v http://localhost:8091?batchSize=10
```

Request will block till:

* timeout occurs - in this case the messages consumed during the period will be returned
* the batch of 10 messages has been consumed.

Example response:

```json
{"messages": [{"key": "key" , "value": {"a" : "b"}}, {"key": "key1" , "value": {"c" : "d"}}]}
```

Endpoint timeouts and consumer groups are configured for every endpoint. It is also possible to commit offsets
explicitly by issuing POST request to the endpoint:

```bash
curl -X POST http://localhost:8091
```

Access to consumer endpoint is serialized and there should be one client talking to one endpoint.

Development
-----------

* Deps
  jdk1.6.0_45

* Build and release

```bash
./sbt release
```
