(ns marzoloco.aggregates.bettor)

(defrecord Bettor
  [bettor-id
   bankroll
   winnings])

(defn apply-event
  [^Bettor bettor event]
  (update-in bettor [:bankroll] + (:amount event)))
