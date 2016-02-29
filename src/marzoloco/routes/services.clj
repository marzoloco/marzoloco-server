(ns marzoloco.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [marzoloco.event-store :as es]
            [marzoloco.read.players :as rp]
            [marzoloco.wagering.commands :as wc]
            [marzoloco.wagering.command-handler :as wch]
            [marzoloco.board-entry.commands :as bc]
            [marzoloco.board-entry.command-handler :as bch]))

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
          {:info {:title "marzoloco-server API"}})

        (context* "/board" []
                  :tags ["Board read model"]
                  )

        (context* "/board-entry/board" []
                  :tags ["Board Entry context, Board commands"]

                  (POST* "/post-game" []
                         :body [cmd bc/PostGame]
                         :summary "Post a Game on the Board"
                         (ok (bch/handle-command event-store cmd)))

                  (POST* "/post-spread-bet" []
                         :body [cmd bc/PostSpreadBet]
                         :summary "Post a Spread Bet to a Game on the Board"
                         (ok (bch/handle-command event-store cmd)))

                  (POST* "/post-total-bet" []
                         :body [cmd bc/PostTotalBet]
                         :summary "Post a Total Bet to a Game on the Board"
                         (ok (bch/handle-command event-store cmd)))

                  (POST* "/post-prop-bet" []
                         :body [cmd bc/PostPropBet]
                         :summary "Post a Prop Bet to a Game on the Board"
                         (ok (bch/handle-command event-store cmd)))

                  (POST* "/post-game-results" []
                         :body [cmd bc/PostGameResults]
                         :summary "Post the completed score of the game to resolve the Bets winners and losers"
                         (ok (bch/handle-command event-store cmd))))

        (context* "/players" []
                  :tags ["Players read model"]

                  (GET* "/" []
                        :summary "Get all Players"
                        (ok (rp/get-players event-store)))

                  (GET* "/:player-id" []
                        :summary "Get Player by id"
                        :path-params [player-id :- s/Uuid]
                        (ok (rp/get-player event-store player-id))))

        (context* "/wagering/player" []
                  :tags ["Wagering context, Player commands"]

                  (POST* "/deposit-points" []
                         :body [cmd wc/DepositPoints]
                         :summary "Deposit points into the player's bankroll"
                         (ok (wch/handle-command event-store cmd)))

                  (POST* "/place-wager" []
                         :body [cmd wc/PlaceWager]
                         :summary "Place a Wager"
                         (ok (wch/handle-command event-store cmd)))

                  (POST* "/withdraw-wager" []
                         :body [cmd wc/WithdrawWager]
                         :summary "Withdraw a Wager if it has not been locked"
                         (ok (wch/handle-command event-store cmd)))

                  (POST* "/cancel-wager" []
                         :body [cmd wc/CancelWager]
                         :summary "Cancel a Wager"
                         (ok (wch/handle-command event-store cmd)))

                  (POST* "/lock-wager" []
                         :body [cmd wc/LockWager]
                         :summary "Lock down a Wager so that it cannot be withdrawn"
                         (ok (wch/handle-command event-store cmd)))

                  (POST* "/close-won-wager" []
                         :body [cmd wc/CloseWonWager]
                         :summary "Close out a Wager that has been won by the Player"
                         (ok (wch/handle-command event-store cmd)))

                  (POST* "/close-pushed-wager" []
                         :body [cmd wc/ClosePushedWager]
                         :summary "Close out a Wager that is a push for the Player"
                         (ok (wch/handle-command event-store cmd)))

                  (POST* "/close-lost-wager" []
                         :body [cmd wc/CloseLostWager]
                         :summary "Close out a Wager that has been Lost by the Player"
                         (ok (wch/handle-command event-store cmd))))

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
