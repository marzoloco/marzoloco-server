(ns marzoloco.betting.bettor
  (:require [schema.core :as s]
            [marzoloco.betting.events :refer :all])
  (:import (marzoloco.betting.events FundsDeposited BetTaken WinningsEarned)))

(defrecord Bettor [bettor-id
                   bankroll
                   winnings])

(defmulti apply-event (fn [_ event]
                        (:event-type event)))

(s/defmethod apply-event :funds-deposited
             [bettor :- Bettor
              event :- FundsDeposited]
             (update-in bettor [:bankroll] + (:amount event)))

(s/defmethod apply-event :bet-taken
             [bettor :- Bettor
              event :- BetTaken]
             (update-in bettor [:bankroll] - (:amount event)))

(s/defmethod apply-event :winnings-earned
             [bettor :- Bettor
              event :- WinningsEarned]
             (update-in bettor [:winnings] + (:amount event)))
