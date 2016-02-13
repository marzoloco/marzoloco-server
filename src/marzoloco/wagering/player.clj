(ns marzoloco.wagering.player
  (:require [schema.core :as s]
            [marzoloco.wagering.events :as e]
            [marzoloco.wagering.commands :as c])
  (:import (marzoloco.wagering.events PointsDeposited WagerPlaced WagerWithdrawn WagerCancelled
                                      WagerLocked WagerWon WagerPushed WagerLost WinningsEarned)
           (marzoloco.wagering.commands  PlaceWager WithdrawWager CancelWager LockWager
                                        CloseWonWager ClosePushedWager CloseLostWager)))


;; The Player aggregate ensures that the player doesn't overdraw their bankroll
;; or withdraw a wager that has been locked.
(defrecord Player [player-id
                   bankroll
                   open-wagers])

(defrecord Wager [wager-id
                  amount
                  odds
                  locked?])


(defn find-open-wager
  [player wager-id]
  (first (filter #(= (:wager-id %) wager-id) (:open-wagers player))))

(defn remove-open-wager
  [player wager-id]
  (let [wager (find-open-wager player wager-id)]
    (update-in player [:open-wagers] #(set (remove #{%2} %1)) wager)))


(defn dispatch-apply-event [player event] (class event))

(defmulti apply-event #'dispatch-apply-event)

(s/defmethod apply-event PointsDeposited
             [player :- Player
              {:keys [amount] :as event} :- PointsDeposited]
             (update-in player [:bankroll] + amount))

(s/defmethod apply-event WagerPlaced
             [player :- Player
              {:keys [wager-id amount odds] :as event} :- WagerPlaced]
             (let [wager (map->Wager {:wager-id wager-id
                                      :amount   amount
                                      :odds     odds
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


(defn dispatch-execute-command [player command] (:command-type command))

(defmulti execute-command #'dispatch-execute-command)

(s/defmethod execute-command :deposit-points
             [{:keys [player-id] :as player} :- Player
              {:keys [amount] :as command} :- c/DepositPoints]
             [(e/map->PointsDeposited {:player-id player-id
                                       :amount    amount})])

(s/defmethod execute-command PlaceWager
             [{:keys [player-id bankroll] :as player} :- Player
              {:keys [wager-id amount odds] :as command} :- PlaceWager]
             (if (<= amount bankroll)
               [(e/map->WagerPlaced {:player-id player-id
                                     :wager-id  wager-id
                                     :amount    amount
                                     :odds      odds})]
               [(e/map->OverdrawAttempted {:player-id player-id
                                           :wager-id  wager-id})]))

(s/defmethod execute-command WithdrawWager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- WithdrawWager]
             (let [wager (find-open-wager player wager-id)]
               (if (:locked? wager)
                 [(e/map->LockedWagerWithdrawAttempted {:player-id player-id
                                                        :wager-id  wager-id})]
                 [(e/map->WagerWithdrawn {:player-id player-id
                                          :wager-id  wager-id})])))

(s/defmethod execute-command CancelWager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- CancelWager]
             [(e/map->WagerCancelled {:player-id player-id
                                      :wager-id  wager-id})])

(s/defmethod execute-command LockWager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- LockWager]
             [(e/map->WagerLocked {:player-id player-id
                                   :wager-id  wager-id})])

(s/defmethod execute-command CloseWonWager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- CloseWonWager]
             (let [{:keys [amount odds] :as wager} (find-open-wager player wager-id)]
               [(e/map->WagerWon {:player-id player-id
                                  :wager-id  wager-id})
                (e/map->WinningsEarned {:player-id player-id
                                        :amount    (* odds amount)})]))

(s/defmethod execute-command ClosePushedWager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- ClosePushedWager]
             (let [wager (find-open-wager player wager-id)]
               [(e/map->WagerPushed {:player-id player-id
                                     :wager-id  wager-id})
                (e/map->WinningsEarned {:player-id player-id
                                        :amount    (:amount wager)})]))

(s/defmethod execute-command CloseLostWager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- CloseLostWager]
             [(e/map->WagerLost {:player-id player-id
                                 :wager-id  wager-id})])