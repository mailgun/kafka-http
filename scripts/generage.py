from bson import BSON
#from bson.binary import Binary

data = {"messages": [{"value": {"a": "b"*500000}}]}
print BSON.encode(data)
