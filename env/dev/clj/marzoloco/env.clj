(ns marzoloco.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [marzoloco.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[marzoloco started successfully using the development profile]=-"))
   :middleware wrap-dev})
