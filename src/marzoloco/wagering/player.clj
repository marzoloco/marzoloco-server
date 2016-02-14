(ns marzoloco.wagering.player
  (:require [schema.core :as s]
            [marzoloco.wagering.events :as e]
            [marzoloco.wagering.commands :as c]))


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


(defn dispatch-apply-event [player event] (:event-type event))

(defmulti apply-event #'dispatch-apply-event)

(s/defmethod apply-event :points-deposited
             [player :- Player
              {:keys [amount] :as event} :- e/PointsDeposited]
             (update-in player [:bankroll] + amount))

(s/defmethod apply-event :wager-placed
             [player :- Player
              {:keys [wager-id amount odds] :as event} :- e/WagerPlaced]
             (let [wager (map->Wager {:wager-id wager-id
                                      :amount   amount
                                      :odds     odds
                                      :locked?  false})]
               (-> player
                   (update-in [:bankroll] - amount)
                   (update-in [:open-wagers] conj wager))))

(s/defmethod apply-event :overdraw-attempted
             [player :- Player
              event :- e/OverdrawAttempted]
             player)

(s/defmethod apply-event :wager-withdrawn
             [player :- Player
              {:keys [wager-id] :as event} :- e/WagerWithdrawn]
             (let [wager (find-open-wager player wager-id)]
               (-> player
                   (update-in [:bankroll] + (:amount wager))
                   (remove-open-wager wager-id))))

(s/defmethod apply-event :locked-wager-withdraw-attempted
             [player :- Player
              event :- e/LockedWagerWithdrawAttempted]
             player)

(s/defmethod apply-event :wager-cancelled
             [player :- Player
              {:keys [wager-id] :as event} :- e/WagerCancelled]
             (let [wager (find-open-wager player wager-id)]
               (-> player
                   (update-in [:bankroll] + (:amount wager))
                   (remove-open-wager wager-id))))

(s/defmethod apply-event :wager-locked
             [player :- Player
              {:keys [wager-id] :as event} :- e/WagerLocked]
             (let [wager (find-open-wager player wager-id)
                   locked-wager (assoc wager :locked? true)]
               (-> player
                   (remove-open-wager wager-id)
                   (update-in [:open-wagers] conj locked-wager))))

(s/defmethod apply-event :wager-won
             [player :- Player
              {:keys [wager-id] :as event} :- e/WagerWon]
             (-> player
                 (remove-open-wager wager-id)))

(s/defmethod apply-event :wager-pushed
             [player :- Player
              {:keys [wager-id] :as event} :- e/WagerPushed]
             (-> player
                 (remove-open-wager wager-id)))

(s/defmethod apply-event :wager-lost
             [player :- Player
              {:keys [wager-id] :as event} :- e/WagerLost]
             (-> player
                 (remove-open-wager wager-id)))

(s/defmethod apply-event :winnings-earned
             [player :- Player
              {:keys [amount] :as event} :- e/WinningsEarned]
             ;; No apparent need for the Player aggregate to store total winnings
             player)


(defn dispatch-execute-command [player command] (:command-type command))

(defmulti execute-command #'dispatch-execute-command)

(s/defmethod execute-command :deposit-points
             [{:keys [player-id] :as player} :- Player
              {:keys [amount] :as command} :- c/DepositPoints]
             [{:event-type :points-deposited
               :player-id  player-id
               :amount     amount}])

(s/defmethod execute-command :place-wager
             [{:keys [player-id bankroll] :as player} :- Player
              {:keys [wager-id amount odds] :as command} :- c/PlaceWager]
             (if (<= amount bankroll)
               [{:event-type :wager-placed
                 :player-id  player-id
                 :wager-id   wager-id
                 :amount     amount
                 :odds       odds}]
               [{:event-type :overdraw-attempted
                 :player-id  player-id
                 :wager-id   wager-id}]))

(s/defmethod execute-command :withdraw-wager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- c/WithdrawWager]
             (let [wager (find-open-wager player wager-id)]
               (if (:locked? wager)
                 [{:event-type :locked-wager-withdraw-attempted
                   :player-id  player-id
                   :wager-id   wager-id}]
                 [{:event-type :wager-withdrawn
                   :player-id  player-id
                   :wager-id   wager-id}])))

(s/defmethod execute-command :cancel-wager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- c/CancelWager]
             [{:event-type :wager-cancelled
               :player-id  player-id
               :wager-id   wager-id}])

(s/defmethod execute-command :lock-wager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- c/LockWager]
             [{:event-type :wager-locked
               :player-id  player-id
               :wager-id   wager-id}])

(s/defmethod execute-command :close-won-wager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- c/CloseWonWager]
             (let [{:keys [amount odds] :as wager} (find-open-wager player wager-id)]
               [{:event-type :wager-won
                 :player-id  player-id
                 :wager-id   wager-id}
                {:event-type :winnings-earned
                 :player-id  player-id
                 :amount     (* odds amount)}]))

(s/defmethod execute-command :close-pushed-wager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- c/ClosePushedWager]
             (let [wager (find-open-wager player wager-id)]
               [{:event-type :wager-pushed
                 :player-id  player-id
                 :wager-id   wager-id}
                {:event-type :winnings-earned
                 :player-id  player-id
                 :amount     (:amount wager)}]))

(s/defmethod execute-command :close-lost-wager
             [{:keys [player-id] :as player} :- Player
              {:keys [wager-id] :as command} :- c/CloseLostWager]
             [{:event-type :wager-lost
               :player-id  player-id
               :wager-id   wager-id}])