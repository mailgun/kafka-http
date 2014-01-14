BROKER_URLS=${KF_PORT_9092_TCP_ADDR}:${KF_PORT_9092_TCP_PORT}

base_dir=$(dirname $0)
$base_dir/kafka-http-endpoint.sh producer --broker-list $BROKER_URLS --sync --request-required-acks 1 --statsd-host $STATSD_PORT_8125_UDP_ADDR --statsd-port $STATSD_PORT_8125_UDP_PORT --statsd-prefix kafka.http.producer
