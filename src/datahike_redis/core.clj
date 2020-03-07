(ns ^{:doc    "Redis backend for datahike"
      :author "J.J. Tolton"}
    datahike-redis.core
  (:require [datahike.store :refer [empty-store
                                    delete-store
                                    connect-store
                                    release-store
                                    scheme->index]]
            [hitchhiker.tree.bootstrap.konserve :as kons]
            [konserve-carmine.core :as rs]
            [superv.async :refer [<?? S]]
            [taoensso.carmine :as car]
            [superv.async :refer [<?? S]]
            [hasch.core :as h])
  (:import (java.net URI)))

(defn ^:private  parse-uri
  "Parse a datahike uri in a carmine options spec.

  Create a carmine connection map from the datahike URI.

  Formats:

  datahike:redis://host
  datahike:redis://host:port
  datahike:redis://doesntmatter:password@host
  datahike:redis://doesntmatter:password@host:port
  datahike:redis://host/db
  datahike:redis://host:port/db
  datahike:redis://doesntmatter:password@host/db
  datahike:redis://doesntmatter:password@host:port/db

  Example formats:
  
  \"datahike:redis://localhost:6379/2\"
  \"datahike:redis://redis-server:snort@localhost:6379/2\"
  \"datahike:redis://redis-server:snort@localhost:6380/3\"
  "
  [uri]
  ;; credit -- adapted from Peter Taoussanis's carmine
  ;; https://github.com/ptaoussanis/carmine/blob/master/src/taoensso/carmine/connections.clj#L197
  (when uri
    (let [uri             (clojure.string/replace-first uri #"datahike[:]" "")
          ^URI uri        (if (instance? URI uri) uri (URI. uri))
          [user password] (.split (str (.getUserInfo uri)) ":")
          port            (.getPort uri)
          db
          (if-let [[_ db-str] (re-matches #"/(\d+)$" (.getPath uri))]
            (Integer. ^String db-str))]
      (-> {:host (.getHost uri)}
          (#(if (pos? port)        (assoc % :port     port)     %))
          (#(if (and db (pos? db)) (assoc % :db       db)       %))
          (#(if password           (assoc % :password password) %))))))


(defn ^:private carmine-conn
  "Create a carmine connection map from the datahike URI.

  Formats:

  datahike:redis://host
  datahike:redis://host:port
  datahike:redis://doesntmatter:password@host
  datahike:redis://doesntmatter:password@host:port
  datahike:redis://host/db
  datahike:redis://host:port/db
  datahike:redis://doesntmatter:password@host/db
  datahike:redis://doesntmatter:password@host:port/db

  Example formats:
  
  \"datahike:redis://localhost:6379/2\"
  \"datahike:redis://redis-server:snort@localhost:6379/2\"
  \"datahike:redis://redis-server:snort@localhost:6380/3\""
  [uri] {:pool {} :spec (parse-uri uri)})

(defmethod empty-store :redis [{:keys [uri] :as opts}]
  (kons/add-hitchhiker-tree-handlers
   (<?? S (rs/new-carmine-store (carmine-conn uri)))))

(defmethod delete-store :redis [{:keys [uri] :as opts}]
  ;; why (str (h/uuid :db)) ?
  ;; tl;dr -- the hitchhiker tree is stored at a key based on uuid created from the keyword :db in redis
  ;; see   https://github.com/replikativ/konserve-carmine/blob/master/src/konserve_carmine/core.clj#L21
  ;; then  https://github.com/replikativ/datahike/blob/master/src/datahike/connector.cljc#L101
  (car/wcar (carmine-conn uri) (car/del (str (h/uuid :db)))))

(defmethod ^{:doc 
"Create a Redis connection from the datahike URI.
Please note that when providing a password for a Redis
connection, the username section is syntactically significant
but the value doesn't matter.  This has been denoted with 
an underscore and with a concrete example of \"doesnmatter\",
as in \"datahike:redis://_:password@host`\" and 
\"datahike:redis://doesntmatter:password@host\".

  Connenction Formats:

  datahike:redis://host
  datahike:redis://host:port
  datahike:redis://_:password@host
  datahike:redis://_:password@host:port
  datahike:redis://doesntmatter:password@host
  datahike:redis://doesntmatter:password@host:port
  datahike:redis://host/db
  datahike:redis://host:port/db
  datahike:redis://_:password@host/db
  datahike:redis://_:password@host:port/db
  datahike:redis://doesntmatter:password@host/db
  datahike:redis://doesntmatter:password@host:port/db

  Example connection format strings:
  
  \"datahike:redis://localhost:6379/2\"
  \"datahike:redis://redis-server:snort@localhost:6379/2\"
  \"datahike:redis://redis-server:snort@localhost:6380/3\""}
  connect-store :redis [{:keys [uri] :as opts}]
  (<?? S (rs/new-carmine-store (carmine-conn uri))))

(defmethod scheme->index :redis [_]
  :datahike.index/hitchhiker-tree)

(defmethod release-store :redis [& args]
  nil)
