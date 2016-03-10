(ns marzoloco.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[marzoloco started successfully]=-"))
   :middleware identity})
