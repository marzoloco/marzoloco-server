(ns marzoloco.wagering.player
  (:require [schema.core :as s]
            [marzoloco.wagering.events :as e])
  (:import (marzoloco.wagering.events PointsDeposited WagerPlaced WagerWithdrawn WagerCancelled
                                      WagerLocked WagerWon WagerPushed WagerLost WinningsEarned)))


;; The Player aggregate ensures that the player doesn't overdraw their bankroll
;; or withdraw a wager that has been locked.
(defrecord Player [player-id
                   bankroll
                   open-wagers])

(defrecord Wager [wager-id
                  amount
                  locked?])


(defn find-open-wager
  [player wager-id]
  (first (filter #(= (:wager-id %) wager-id) (:open-wagers player))))

(defn remove-open-wager
  [player wager-id]
  (let [wager (find-open-wager player wager-id)]
    (update-in player [:open-wagers] #(set (remove #{%2} %1)) wager)))


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

(s/defmethod apply-event WagerWithdrawn
             [player :- Player
              {:keys [wager-id] :as event} :- WagerWithdrawn]
             (let [wager (find-open-wager player wager-id)]
               (-> player
                   (update-in [:bankroll] + (:amount wager))
                   (remove-open-wager wager-id))))

(s/defmethod apply-event WagerCancelled
             [player :- Player
              {:keys [wager-id] :as event} :- WagerCancelled]
             (let [wager (find-open-wager player wager-id)]
               (-> player
                   (update-in [:bankroll] + (:amount wager))
                   (remove-open-wager wager-id))))

(s/defmethod apply-event WagerLocked
             [player :- Player
              {:keys [wager-id] :as event} :- WagerLocked]
             (let [wager (find-open-wager player wager-id)
                   locked-wager (assoc wager :locked? true)]
               (-> player
                   (remove-open-wager wager-id)
                   (update-in [:open-wagers] conj locked-wager))))

(s/defmethod apply-event WagerWon
             [player :- Player
              {:keys [wager-id] :as event} :- WagerWon]
             (-> player
                 (remove-open-wager wager-id)))

(s/defmethod apply-event WagerPushed
             [player :- Player
              {:keys [wager-id] :as event} :- WagerPushed]
             (-> player
                 (remove-open-wager wager-id)))

(s/defmethod apply-event WagerLost
             [player :- Player
              {:keys [wager-id] :as event} :- WagerLost]
             (-> player
                 (remove-open-wager wager-id)))

(s/defmethod apply-event WinningsEarned
             [player :- Player
              {:keys [amount] :as event} :- WinningsEarned]
             ;; No apparent need for the Player aggregate to store total winnings
             player)