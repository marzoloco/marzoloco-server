(ns marzoloco.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [marzoloco.wagering.commands :as c]
            [marzoloco.wagering.player :as p]))

(s/defschema Thingie {:id    Long
                      :hot   Boolean
                      :tag   (s/enum :kikka :kukka)
                      :chief [{:name String
                               :type #{{:id String}}}]})

(defapi service-routes
        (ring.swagger.ui/swagger-ui
          "/swagger-ui")
        ;JSON docs available at the /swagger.json route
        (swagger-docs
          {:info {:title "marzoloco-server api"}})

        (context* "/wagering/player/command" []
                  :tags ["Execute commands against a Player in the Wagering context"]

                  (POST* "/deposit-points" []
                         :body [cmd c/DepositPoints]
                         :summary "Deposit points into the player's bankroll"
                         (ok (let [{player-id :player-id} cmd
                                   player (p/map->Player {:player-id player-id
                                                          :bankroll  23})]
                               (p/execute-command player cmd)))))

        (context* "/api" []
                  :tags ["z sample - thingie"]

                  (GET* "/plus" []
                        :return Long
                        :query-params [x :- Long, {y :- Long 1}]
                        :summary "x+y with query-parameters. y defaults to 1."
                        (ok (+ x y)))

                  (POST* "/minus" []
                         :return Long
                         :body-params [x :- Long, y :- Long]
                         :summary "x-y with body-parameters."
                         (ok (- x y)))

                  (GET* "/times/:x/:y" []
                        :return Long
                        :path-params [x :- Long, y :- Long]
                        :summary "x*y with path-parameters"
                        (ok (* x y)))

                  (POST* "/divide" []
                         :return Double
                         :form-params [x :- Long, y :- Long]
                         :summary "x/y with form-parameters"
                         (ok (/ x y)))

                  (GET* "/power" []
                        :return Long
                        :header-params [x :- Long, y :- Long]
                        :summary "x^y with header-parameters"
                        (ok (long (Math/pow x y))))

                  (PUT* "/echo" []
                        :return [{:hot Boolean}]
                        :body [body [{:hot Boolean}]]
                        :summary "echoes a vector of anonymous hotties"
                        (ok body))

                  (POST* "/echo" []
                         :return (s/maybe Thingie)
                         :body [thingie (s/maybe Thingie)]
                         :summary "echoes a Thingie from json-body"
                         (ok thingie)))

        (context* "/context" []
                  :tags ["z sample - context*"]
                  :summary "summary inherited from context"
                  (context* "/:kikka" []
                            :path-params [kikka :- s/Str]
                            :query-params [kukka :- s/Str]
                            (GET* "/:kakka" []
                                  :path-params [kakka :- s/Str]
                                  (ok {:kikka kikka
                                       :kukka kukka
                                       :kakka kakka})))))
