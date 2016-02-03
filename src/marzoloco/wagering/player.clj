(ns marzoloco.wagering.player
  (:require [schema.core :as s]
            [marzoloco.wagering.events :as e])
  (:import (marzoloco.wagering.events PointsDeposited WagerPlaced WinningsEarned WagerWon)))

(defrecord Player [player-id
                   bankroll
                   open-wagers
                   winnings])

(defmulti apply-event (fn [_ event] (class event)))

(s/defmethod apply-event PointsDeposited
             [player :- Player
              {:keys [amount] :as event} :- PointsDeposited]
             (update-in player [:bankroll] + amount))

(s/defmethod apply-event WagerPlaced
             [player :- Player
              {:keys [amount wager-id] :as event} :- WagerPlaced]
             (-> player
                 (update-in [:bankroll] - amount)
                 (update-in [:open-wagers] conj wager-id)))

(s/defmethod apply-event WagerWon
             [player :- Player
              {:keys [wager-id] :as event} :- WagerWon]
             (-> player
                 (update-in [:open-wagers] #(remove #{%2} %1) wager-id)))

(s/defmethod apply-event WinningsEarned
             [player :- Player
              {:keys [amount] :as event} :- WinningsEarned]
             (update-in player [:winnings] + amount))
