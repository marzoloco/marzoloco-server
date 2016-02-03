(ns marzoloco.wagering.player
  (:require [schema.core :as s]
            [marzoloco.wagering.events :as e])
  (:import (marzoloco.wagering.events PointsDeposited WagerPlaced WinningsEarned WagerWon WagerPushed WagerLost)))

(defrecord Player [player-id
                   bankroll
                   open-wagers
                   winnings])

(defrecord Wager [wager-id
                  amount
                  locked?])

(defn dispatch-apply-event [aggregate event] (class event))

(defmulti apply-event #'dispatch-apply-event)

(s/defmethod apply-event PointsDeposited
             [player :- Player
              {:keys [amount] :as event} :- PointsDeposited]
             (update-in player [:bankroll] + amount))

(s/defmethod apply-event WagerPlaced
             [player :- Player
              {:keys [amount wager-id] :as event} :- WagerPlaced]
             (let [wager (map->Wager {:wager-id wager-id
                                      :amount   amount
                                      :locked?  false})]
               (-> player
                   (update-in [:bankroll] - amount)
                   (update-in [:open-wagers] conj wager))))

(defn remove-from-open-wagers
  [player wager-id]
  (let [wager (first (filter #(= (:wager-id %) wager-id) (:open-wagers player)))]
    (update-in player [:open-wagers] #(set (remove #{%2} %1)) wager)))

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
