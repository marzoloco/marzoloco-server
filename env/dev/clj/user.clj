(ns user
  (:require [mount.core :as mount]
            marzoloco.core))

(defn start []
  (mount/start-without #'marzoloco.core/repl-server))

(defn stop []
  (mount/stop-except #'marzoloco.core/repl-server))

(defn restart []
  (stop)
  (start))


