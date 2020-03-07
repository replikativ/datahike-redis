# datahike-redis

[Datahike](https://github.com/replikativ/datahike) with [Redis](https://github.com/antirez/redis/) as data storage.

## Usage

After including the datahike API and the datahike-redis namespace, you can use the Redis backend now using the keyword `:redis`

```clojure
(ns project.core
  (:require [datahike.api :as d]
            [datahike-redis.core]))

;; Use a redis URI as a configuration
(def config "datahike:redis://localhost:6379")

;; Create a database at this place, by default configuration we have a strict
;; schema and temporal index
(d/create-database config)

(def conn (d/connect config))

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
(d/delete-database config)
```


## License

Copyright © 2020  lambdaforge UG (haftungsbeschränkt)

This program and the accompanying materials are made available under the terms of the Eclipse Public License 1.0.
