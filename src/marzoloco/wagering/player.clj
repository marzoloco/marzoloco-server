(ns marzoloco.wagering.player
  (:require [schema.core :as s]
            [marzoloco.wagering.events :as e])
  (:import (marzoloco.wagering.events PointsDeposited WagerPlaced WinningsEarned)))

(defrecord Player [player-id
                   bankroll
                   winnings])

(defmulti apply-event (fn [_ event] (class event)))

(s/defmethod apply-event PointsDeposited
             [player :- Player
              event :- PointsDeposited]
             (update-in player [:bankroll] + (:amount event)))

(s/defmethod apply-event WagerPlaced
             [player :- Player
              event :- WagerPlaced]
             (update-in player [:bankroll] - (:amount event)))

(s/defmethod apply-event WinningsEarned
             [player :- Player
              event :- WinningsEarned]
             (update-in player [:winnings] + (:amount event)))
