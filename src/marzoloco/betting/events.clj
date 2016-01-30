(ns marzoloco.betting.events
  (:require [schema.core :as s]))

(s/defrecord FundsDeposited [event-type :- (s/eq :funds-deposited)
                             bettor-id :- s/Str
                             amount :- BigDecimal])
