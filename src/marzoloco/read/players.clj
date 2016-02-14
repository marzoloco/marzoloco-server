(ns marzoloco.read.players
  (:require [schema.core :as s]
            [marzoloco.wagering.events :as we]))

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
   :open-wagers #{}})

(defn dispatch-apply-event [players event] (:event-type event))

(defmulti apply-event #'dispatch-apply-event)

(s/defmethod apply-event :points-deposited
             [players
              {:keys [player-id amount] :as event} :- we/PointsDeposited]
             ;; For now, this applier takes on the task of initializing the player. Eventually
             ;; we'll have an explicit event for this coming from some context other than Wagering.
             (let [{player player-id :or {player (make-initial-player player-id)}} players
                   updated-player (-> player
                                      (update-in [:bankroll] + amount))]
               (assoc players player-id updated-player)))
