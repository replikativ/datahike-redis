# Datahike Redis Backend

<p align="center">
<a href="https://clojurians.slack.com/archives/CB7GJAN0L"><img src="https://img.shields.io/badge/clojurians%20slack-join%20channel-blueviolet"/></a>
<a href="https://clojars.org/io.replikativ/datahike-redis"> <img src="https://img.shields.io/clojars/v/io.replikativ/datahike-redis.svg" /></a>
<a href="https://circleci.com/gh/replikativ/datahike-redis"><img src="https://circleci.com/gh/replikativ/datahike-redis.svg?style=shield"/></a>
<a href="https://github.com/replikativ/datahike-redis/tree/main"><img src="https://img.shields.io/github/last-commit/replikativ/datahike-redis/main"/></a>
</p>

The goal of this backend is to support [Redis](https://redis.io).

## Configuration
Please read the [Datahike configuration docs](https://github.com/replikativ/datahike/blob/master/doc/config.md) on how to configure your backend. Details about the backend configuration can be found in [konserve-redis](https://github.com/replikativ/konserve-redis).A sample configuration is
`create-database`, `connect` and `delete-database`:
```clojure
{:store {:backend :redis
         :uri "redis://localhost:9475"}}
```
This same configuration can be achieved by setting one environment variable for the redis backend
and one environment variable for the configuration of the redis backend:
```bash
DATAHIKE_STORE_BACKEND=redis
DATAHIKE_STORE_CONFIG='{:uri "redis://localhost:9475"}'
```

## Usage
Add to your Leiningen or Boot dependencies:
[![Clojars Project](https://img.shields.io/clojars/v/io.replikativ/datahike-redis.svg)](https://clojars.org/io.replikativ/datahike-redis)

Now require the Datahike API and the datahike-redis namespace in your editor or REPL using the
keyword `:redis`. If you want to use other backends than S3 please refer to the official
[Datahike docs](https://github.com/replikativ/datahike/blob/master/doc/config.md).

### Run Datahike in your REPL
```clojure
  (ns project.core
    (:require [datahike.api :as d]
              [datahike-redis.core]))

  (def cfg {:store {:backend :redis
                    :bucket "redis://localhost:9475"}})

  ;; Create a database at this place, by default configuration we have a strict
  ;; schema validation and keep historical data
  (d/create-database cfg)

  (def conn (d/connect cfg))

  ;; The first transaction will be the schema we are using:
  (d/transact conn [{:db/ident :name
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one }
                    {:db/ident :age
                     :db/valueType :db.type/long
                     :db/cardinality :db.cardinality/one }])

  ;; Let's add some data and wait for the transaction
  (d/transact conn [{:name  "Alice", :age   20 }
                    {:name  "Bob", :age   30 }
                    {:name  "Charlie", :age   40 }
                    {:age 15 }])

  ;; Search the data
  (d/q '[:find ?e ?n ?a
         :where
         [?e :name ?n]
         [?e :age ?a]]
    @conn)
  ;; => #{[3 "Alice" 20] [4 "Bob" 30] [5 "Charlie" 40]}

  ;; Clean up the database if it is not needed any more
  (d/delete-database cfg)
```

## Run Tests

```bash
  bash -x ./bin/run-integration-tests
```

## License

Copyright © 2023 lambdaforge UG (haftungsbeschränkt)

This program and the accompanying materials are made available under the terms of the Eclipse Public License 1.0.
