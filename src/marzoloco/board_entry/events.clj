(ns marzoloco.board-entry.events
  (:require [schema.core :as s]))

(s/defschema GamePosted {:event-type  (s/eq :game-posted)
                         :board-id    s/Uuid
                         :game-id     s/Uuid
                         :team-a-name s/Str
                         :team-b-name s/Str})

(s/defschema SpreadBet {:bet-id   s/Uuid

                        :bet-type (s/eq :spread-bet)
                        :favorite (s/enum :team-a :team-b)
                        :spread   s/Num})

(s/defschema TotalBet {:bet-id     s/Uuid
                       :bet-type   (s/eq :total-bet)
                       :over-under s/Num})

(s/defschema PropBet {:bet-id     s/Uuid
                      :bet-type   (s/eq :prop-bet)
                      :over-under s/Num})

(s/defschema BetPosted {:event-type (s/eq :bet-posted)
                        :board-id   s/Uuid
                        :game-id    s/Uuid
                        :bet        (s/either SpreadBet TotalBet PropBet)})

(def Side (s/enum :favorite :underdog :over :under))

(s/defschema SideWon {:event-type   (s/eq :side-won)
                      :board-id     s/Uuid
                      :game-id      s/Uuid
                      :bet-id       s/Uuid
                      :winning-side Side})

(s/defschema SideLost {:event-type  (s/eq :side-lost)
                       :board-id    s/Uuid
                       :game-id     s/Uuid
                       :bet-id      s/Uuid
                       :losing-side Side})