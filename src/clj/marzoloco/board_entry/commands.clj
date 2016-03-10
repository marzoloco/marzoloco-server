(ns marzoloco.board-entry.commands
  (:require [schema.core :as s]))

(s/defschema PostGame {:command-type (s/eq :post-game)
                       :board-id     s/Uuid
                       :game-id      s/Uuid
                       :team-a-name  s/Str
                       :team-b-name  s/Str})

(s/defschema PostSpreadBet {:command-type (s/eq :post-spread-bet)
                            :board-id     s/Uuid
                            :game-id      s/Uuid
                            :bet-id       s/Uuid
                            :favorite     (s/enum :team-a :team-b)
                            :spread       s/Num})

(s/defschema PostTotalBet {:command-type (s/eq :post-total-bet)
                           :board-id     s/Uuid
                           :game-id      s/Uuid
                           :bet-id       s/Uuid
                           :over-under   s/Num})

(s/defschema PostPropBet {:command-type (s/eq :post-prop-bet)
                          :board-id     s/Uuid
                          :game-id      s/Uuid
                          :bet-id       s/Uuid
                          :over-under   s/Num})

(s/defschema PostGameResults {:command-type  (s/eq :post-game-results)
                              :board-id      s/Uuid
                              :game-id       s/Uuid
                              :team-a-points s/Int
                              :team-b-points s/Int})
