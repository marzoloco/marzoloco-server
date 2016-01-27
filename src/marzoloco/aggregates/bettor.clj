(ns marzoloco.aggregates.bettor)

(defn apply-event
  [bettor-agg event]
  (update-in bettor-agg [:bankroll] + (:amount event)))
