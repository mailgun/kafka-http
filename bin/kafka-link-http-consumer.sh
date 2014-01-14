TOPIC=messages
ZOOKEEPER_URLS=${ZK_PORT_2181_TCP_ADDR}:${ZK_PORT_2181_TCP_PORT}
GROUP=hammer

base_dir=$(dirname $0)
$base_dir/kafka-http-endpoint.sh consumer --group $GROUP --topic $TOPIC --zookeeper $ZOOKEEPER_URLS --consumer-timeout-ms 5000
