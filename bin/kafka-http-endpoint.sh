base_dir=$(dirname $0)
export KAFKA_OPTS="-Xmx2048M -server -Dcom.sun.management.jmxremote -Dlog4j.configuration=file:$base_dir/kafka-endpoint-log4j.properties -Dorg.eclipse.jetty.io.ChanelEndPoint.LEVEL=ALL"

for file in $base_dir/../*.jar;
do
  CLASSPATH=$CLASSPATH:$file
done
export CLASSPATH="$CLASSPATH"

$base_dir/kafka-run-class.sh com.rackspace.kafka.http.RestServer $@
