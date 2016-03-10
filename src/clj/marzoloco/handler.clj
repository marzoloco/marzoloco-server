(ns marzoloco.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [marzoloco.layout :refer [error-page]]
            [marzoloco.routes.home :refer [home-routes]]
            [marzoloco.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [marzoloco.middleware :as middleware]))

(def app-routes
  (routes
    #'service-routes
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))