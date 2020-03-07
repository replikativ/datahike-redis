(ns datahike-redis.core-test
  (:require [datahike-redis.core :as dr]
            [datahike.api :as d]
            [clojure.test :as t :refer :all]))

(def ^:const redis-uri "datahike:redis://localhost:6379")
(def ^:const muffalo "muffalo")

;; todo -- needs a travis or circle-ci setup
#_
(deftest test-redis-store
  (testing "redis-store integration test"
    (try
      (d/create-database redis-uri)
      (catch clojure.lang.ExceptionInfo _
        (d/delete-database redis-uri)
        (d/create-database redis-uri)))
    (let [conn (d/connect redis-uri)]
      (d/transact conn [{:db/ident       :thing/name
                         :db/unique      :db.unique/identity
                         :db/cardinality :db.cardinality/one
                         :db/valueType   :db.type/string}])
      (d/transact conn [{:thing/name muffalo}])
      (let [{id   :db/id
             name :thing/name}
            (d/q '[:find (pull ?e [*]) .
                   :in $ ?muffalo
                   :where
                   [?e :thing/name ?muffalo]]
                 @conn
                 muffalo)]
        (is (= name muffalo))
        (is (= (:thing/name (d/pull @conn '[*] id))
               muffalo))))
    (is (= 1 (d/delete-database redis-uri)))
    (is (nil?  (d/create-database redis-uri)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (d/create-database redis-uri)))
    (is (= 1 (d/delete-database redis-uri)))
    (is (zero? (d/delete-database redis-uri)))))

(deftest test-redis-helpers
  (testing "testing redis helper utilities"
    (let [parse-uri #'dr/parse-uri]
      (is (= (parse-uri "datahike:redis://localhost:6379/2")
             {:host "localhost", :port 6379, :db 2}))
      (is (= (parse-uri "datahike:redis://redis-server:snort@localhost:6379/2")
             {:host "localhost", :port 6379, :db 2, :password "snort"}))
      (is (= (parse-uri "datahike:redis://redis-server:snort@localhost:6380/3")
             {:host "localhost", :port 6380, :db 3, :password "snort"})))))

(defn -main []
  (run-tests))
