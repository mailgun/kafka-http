base_dir=$(dirname $0)
export KAFKA_OPTS="-Xmx512M -server -Dcom.sun.management.jmxremote -Dlog4j.configuration=file:$base_dir/kafka-console-consumer-log4j.properties"

for file in $base_dir/../*.jar;
do
  CLASSPATH=$CLASSPATH:$file
done
export CLASSPATH="$CLASSPATH"

$base_dir/kafka-run-class.sh kafka.rest.RestServer $@
