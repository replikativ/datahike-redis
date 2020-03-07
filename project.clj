(defproject io.replikativ/datahike-redis "0.1.0-SNAPSHOT"
  :description "Datahike with Redis as data storage."
  :license {:name "Eclipse"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :url "https://github.com/replikativ/datahike-redis"
  :plugins [[lein-tools-deps "0.4.1"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}

  :aliases {"test-clj" ["run" "-m" "datahike-redis.test/core_test-clj"]
            "test-all" ["do" ["clean"] ["test-clj"]]}

  :profiles {:dev {:source-paths ["test"]
                   :dependencies [[org.clojure/tools.nrepl     "0.2.13"]
                                  [org.clojure/tools.namespace "0.3.1"]]}})
