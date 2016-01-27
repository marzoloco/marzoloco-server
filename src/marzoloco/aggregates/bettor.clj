(ns marzoloco.aggregates.bettor)

(defrecord Bettor
  [bettor-id
   bankroll
   winnings])

(defmulti apply-event (fn [^Bettor bettor event]
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
