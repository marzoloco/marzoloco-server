(ns marzoloco.config
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[marzoloco started successfully]=-"))
   :middleware identity})
