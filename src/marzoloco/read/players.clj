(ns marzoloco.read.players
  (:require [schema.core :as s]
            [com.rpl.specter :refer :all]
            [marzoloco.wagering.events :as we]
            [marzoloco.event-store :as es]))

;;
;; Consuming all player-related events to build a
;; read model showing info for all players.
;;
;; Gonna start out with no schema constraints, see hot it goes.
;;
;; Also gonna try out REPL-driven development here, rather than TDD.
;;


(defn make-initial-player
  [player-id]
  {:player-id   player-id
   :bankroll    0
   :open-wagers #{}
   :winnings    0})


(defn remove-open-wager [player-id wager-id players]
  (transform [(keypath player-id)
              (collect-one [:open-wagers ALL #(= (:wager-id %) wager-id)])
              :open-wagers]
             #(disj %2 %1) players))


(defn dispatch-apply-event [players event] (:event-type event))

(defmulti apply-event #'dispatch-apply-event)

(s/defmethod apply-event :points-deposited
  [players
   {:keys [player-id amount] :as event} :- we/PointsDeposited]
  ;; For now, this applier takes on the task of initializing the player. Eventually
  ;; we'll have an explicit event for this coming from some context other than Wagering.
  (let [seeded-players (if (contains? players player-id)
                         players
                         (assoc players player-id (make-initial-player player-id)))]
    (transform [(keypath player-id) :bankroll] #(+ % amount) seeded-players)))

(s/defmethod apply-event :wager-placed
  [players
   {:keys [player-id amount] :as event} :- we/WagerPlaced]
  (let [open-wager (select-keys event [:wager-id :amount :odds])]
    (->> players
         (transform [(keypath player-id) :bankroll] #(- % amount))
         (transform [(keypath player-id) :open-wagers] #(conj % open-wager)))))

(s/defmethod apply-event :overdraw-attempted
  [players event :- we/OverdrawAttempted]
  players)

(s/defmethod apply-event :wager-withdrawn
  [players
   {:keys [player-id wager-id] :as event} :- we/WagerWithdrawn]
  (->> players
       (transform [(keypath player-id)
                   (collect-one [:open-wagers ALL #(= (:wager-id %) wager-id) :amount])
                   :bankroll]
                  +)
       (remove-open-wager player-id wager-id)))

(s/defmethod apply-event :locked-wager-withdraw-attempted
  [players event :- we/OverdrawAttempted]
  players)

(s/defmethod apply-event :wager-cancelled
  [players
   {:keys [player-id wager-id] :as event} :- we/WagerWithdrawn]
  (->> players
       (transform [(keypath player-id)
                   (collect-one [:open-wagers ALL #(= (:wager-id %) wager-id) :amount])
                   :bankroll]
                  +)
       (transform [(keypath player-id)
                   (collect-one [:open-wagers ALL #(= (:wager-id %) wager-id)])
                   :open-wagers]
                  #(disj %2 %1))))

(s/defmethod apply-event :wager-locked
  [players
   {:keys [player-id wager-id] :as event} :- we/WagerLocked]
  (->> players
       (setval [(keypath player-id) :open-wagers ALL #(= (:wager-id %) wager-id) :locked?] true)))

(s/defmethod apply-event :wager-won
  [players
   {:keys [player-id wager-id] :as event} :- we/WagerWon]
  (remove-open-wager player-id wager-id players))

(s/defmethod apply-event :wager-pushed
  [players
   {:keys [player-id wager-id] :as event} :- we/WagerWon]
  (remove-open-wager player-id wager-id players))

(s/defmethod apply-event :wager-lost
  [players
   {:keys [player-id wager-id] :as event} :- we/WagerWon]
  (remove-open-wager player-id wager-id players))

(s/defmethod apply-event :winnings-earned
  [players
   {:keys [player-id amount] :as event} :- we/WinningsEarned]
  (->> players
       (transform [(keypath player-id) :winnings] #(+ % amount))))


(defn make-players
  [events]
  (reduce apply-event {} events))

(defn get-players
  [event-store]
  (->> event-store
       es/get-all-events
       make-players
       vals))

(defn get-player
  [event-store player-id]
  (->> event-store
       es/get-all-events
       (filter #(= (:player-id %) player-id))
       make-players
       vals
       first))
