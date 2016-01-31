(ns marzoloco.betting.events
  (:require [schema.core :as s]))

(s/defrecord FundsDeposited [bettor-id :- s/Str
                             amount :- BigDecimal])

(s/defrecord BetTaken [bettor-id :- s/Str
                       amount :- BigDecimal])

(s/defrecord WinningsEarned [bettor-id :- s/Str
                             amount :- BigDecimal])
