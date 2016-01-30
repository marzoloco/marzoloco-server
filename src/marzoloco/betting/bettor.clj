(ns marzoloco.betting.bettor
  (:require [schema.core :as s]))

(s/defrecord Bettor
  [bettor-id :- s/Str
   bankroll :- BigDecimal
   winnings :- BigDecimal])

(defmulti apply-event (fn [_ event]
                        (:event-type event)))

(defmethod apply-event :funds-deposited
  [^Bettor bettor event]
  (update-in bettor [:bankroll] + (:amount event)))

(defmethod apply-event :bet-taken
  [^Bettor bettor event]
  (update-in bettor [:bankroll] - (:amount event)))

(defmethod apply-event :winnings-earned
  [^Bettor bettor event]
  (update-in bettor [:winnings] + (:amount event)))
