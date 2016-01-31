(ns marzoloco.betting.events
  (:require [schema.core :as s]))

(s/defrecord FundsDeposited [event-type :- (s/eq :funds-deposited)
                             bettor-id :- s/Str
                             amount :- BigDecimal])

(s/defrecord BetTaken [event-type :- (s/eq :bet-taken)
                       bettor-id :- s/Str
                       amount :- BigDecimal])

(s/defrecord WinningsEarned [event-type :- (s/eq :winnings-earned)
                             bettor-id :- s/Str
                             amount :- BigDecimal])
