(ns marzoloco.wagering.player
  (:require [schema.core :as s]
            [marzoloco.wagering.events :as e])
  (:import (marzoloco.wagering.events PointsDeposited WagerPlaced WinningsEarned WagerWon WagerPushed WagerLost)))

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

(defn remove-from-open-wagers
  [player wager-id]
  (update-in player [:open-wagers] #(set (remove #{%2} %1)) wager-id))

(s/defmethod apply-event WagerWon
             [player :- Player
              {:keys [wager-id] :as event} :- WagerWon]
             (-> player
                 (remove-from-open-wagers wager-id)))

(s/defmethod apply-event WagerPushed
             [player :- Player
              {:keys [wager-id] :as event} :- WagerPushed]
             (-> player
                 (remove-from-open-wagers wager-id)))

(s/defmethod apply-event WagerLost
             [player :- Player
              {:keys [wager-id] :as event} :- WagerLost]
             (-> player
                 (remove-from-open-wagers wager-id)))

(s/defmethod apply-event WinningsEarned
             [player :- Player
              {:keys [amount] :as event} :- WinningsEarned]
             (update-in player [:winnings] + amount))
