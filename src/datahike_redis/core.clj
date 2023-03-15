(ns datahike-redis.core
  (:require [datahike.store :refer [empty-store delete-store connect-store default-config config-spec release-store store-identity]]
            [datahike.config :refer [map-from-env]]
            [konserve-redis.core :as k]
            [clojure.spec.alpha :as s]))

(defmethod store-identity :redis [store-config]
  [:redis (:uri store-config)])

(defmethod empty-store :redis [store-config]
  (k/connect-store store-config))

(defmethod delete-store :redis [store-config]
  (k/delete-store store-config))

(defmethod connect-store :redis [store-config]
  (k/connect-store store-config))

(defmethod default-config :redis [config]
  (merge
   (map-from-env :datahike-store-config {})
   config))

(s/def :datahike.store.redis/backend #{:redis})
(s/def :datahike.store.redis/uri string?)
(s/def ::redis (s/keys :req-un [:datahike.store.redis/backend]
                       :opt-un [:datahike.store.redis/uri]))

(defmethod config-spec :redis [_] ::redis)

(defmethod release-store :redis [_ store]
  (k/release store {:sync? true}))
