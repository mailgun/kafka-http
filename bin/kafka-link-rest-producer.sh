BROKER_URLS=${KF_PORT_9092_TCP_ADDR}:${KF_PORT_9092_TCP_PORT}

base_dir=$(dirname $0)
$base_dir/kafka-rest-endpoint.sh producer --broker-list $BROKER_URLS --sync --request-required-acks 1
