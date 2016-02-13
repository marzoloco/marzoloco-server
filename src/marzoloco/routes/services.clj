(ns marzoloco.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [marzoloco.wagering.commands :as c]
            [marzoloco.event-store :as es]
            [marzoloco.wagering.command-handler :as ch]))

(s/defschema Thingie {:id    Long
                      :hot   Boolean
                      :tag   (s/enum :kikka :kukka)
                      :chief [{:name String
                               :type #{{:id String}}}]})

(def event-store (es/make-in-memory-event-store))

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
                         (ok (ch/handle-command event-store cmd)))

                  (POST* "/place-wager" []
                         :body [cmd c/PlaceWager]
                         :summary "Place a Wager"
                         (ok (ch/handle-command event-store cmd)))

                  (POST* "/withdraw-wager" []
                         :body [cmd c/WithdrawWager]
                         :summary "Withdraw a Wager if it has not been locked"
                         (ok (ch/handle-command event-store cmd)))

                  (POST* "/cancel-wager" []
                         :body [cmd c/CancelWager]
                         :summary "Cancel a Wager"
                         (ok (ch/handle-command event-store cmd)))

                  (POST* "/lock-wager" []
                         :body [cmd c/LockWager]
                         :summary "Lock down a Wager so that it cannot be withdrawn"
                         (ok (ch/handle-command event-store cmd)))

                  (POST* "/close-won-wager" []
                         :body [cmd c/CloseWonWager]
                         :summary "Close out a Wager that has been won by the Player"
                         (ok (ch/handle-command event-store cmd)))

                  (POST* "/close-pushed-wager" []
                         :body [cmd c/ClosePushedWager]
                         :summary "Close out a Wager that is a push for the Player"
                         (ok (ch/handle-command event-store cmd)))

                  (POST* "/close-lost-wager" []
                         :body [cmd c/CloseLostWager]
                         :summary "Close out a Wager that has been Lost by the Player"
                         (ok (ch/handle-command event-store cmd))))

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
