#!/bin/bash

echo "🔁 Waiting for MongoDB instances to be ready..."

wait_for_mongo() {
    local HOST=$1
    until mongosh --host "$HOST" --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
        echo -n "."
        sleep 1
    done
    echo "✅ $HOST is ready"
}

wait_for_mongo "mongo1:27017"
wait_for_mongo "mongo2:27017"
wait_for_mongo "mongo3:27017"

echo "Initiating replica set..."

mongosh --host mongo1:27017 <<EOF
    rs.initiate({
        _id: "rs0",
        members: [
          { _id: 0, host: "mongo1:27017" },
          { _id: 1, host: "mongo2:27017" },
          { _id: 2, host: "mongo3:27017" }
        ]
      }
    );
EOF

echo "✅ Replica set initialized."