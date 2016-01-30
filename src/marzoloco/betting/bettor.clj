(ns marzoloco.betting.bettor
  (:require [schema.core :as s]
            [marzoloco.betting.events :refer :all])
  (:import (marzoloco.betting.events FundsDeposited)))

(defrecord Bettor [bettor-id
                   bankroll
                   winnings])

(defmulti apply-event (fn [_ event]
                        (:event-type event)))

(s/defmethod apply-event :funds-deposited
             [bettor :- Bettor
              event :- FundsDeposited]
             (update-in bettor [:bankroll] + (:amount event)))

(defmethod apply-event :bet-taken
  [^Bettor bettor event]
  (update-in bettor [:bankroll] - (:amount event)))

(defmethod apply-event :winnings-earned
  [^Bettor bettor event]
  (update-in bettor [:winnings] + (:amount event)))
