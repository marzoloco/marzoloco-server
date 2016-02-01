(ns marzoloco.wagering.events
  (:require [schema.core :as s]))

(s/defrecord PointsDeposited [player-id :- s/Uuid
                              amount :- BigDecimal])

(s/defrecord WagerPlaced [player-id :- s/Uuid
                          wager-id :- s/Uuid
                          amount :- BigDecimal])

(s/defrecord WinningsEarned [player-id :- s/Uuid
                             amount :- BigDecimal])
