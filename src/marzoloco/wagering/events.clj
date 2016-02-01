(ns marzoloco.wagering.events
  (:require [schema.core :as s]))

(s/defrecord PointsDeposited [player-id :- s/Str
                              amount :- BigDecimal])

(s/defrecord WagerPlaced [player-id :- s/Str
                          amount :- BigDecimal])

(s/defrecord WinningsEarned [player-id :- s/Str
                             amount :- BigDecimal])
