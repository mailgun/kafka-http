FILENAME=session${SIZE}.log
echo "Using session sample: $FILENAME"

#httperf --hog --client=0/1 --server=localhost --port=8080 --uri=/ --add-header='Content-Type:application/json\nAccept:application/json\n' --method=POST --wsesslog=6000,1,${FILENAME} --max-piped-calls 10 --rate 600
httperf --hog --client=0/1 --server=localhost --port=8080 --add-header='Content-Type:application/bson\nAccept:application/json\n' --wsesslog=200,1,${FILENAME} --max-piped-calls 10 --rate 200
