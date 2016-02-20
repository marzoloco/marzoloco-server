(ns marzoloco.board-entry.events
  (:require [schema.core :as s]))

(s/defschema GamePosted {:event-type  (s/eq :game-posted)
                         :board-id    s/Uuid
                         :game-id     s/Uuid
                         :team-a-name s/Str
                         :team-b-name s/Str})

(s/defschema SpreadBet {:bet-id        s/Uuid
                        :bet-type      (s/eq :spread-bet)
                        :favorite-side (s/enum :team-a :team-b)
                        :spread        s/Int})

(s/defschema TotalBet {:bet-id     s/Uuid
                       :bet-type   (s/eq :total-bet)
                       :over-under s/Int})

(s/defschema PropBet {:bet-id     s/Uuid
                      :bet-type   (s/eq :prop-bet)
                      :over-under s/Int})

(s/defschema BetPosted {:event-type (s/eq :bet-posted)
                        :board-id   s/Uuid
                        :game-id    s/Uuid
                        :bet        (s/either SpreadBet TotalBet PropBet)})