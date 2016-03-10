(ns marzoloco.wagering.command-handler
  (:require [marzoloco.event-store :as es]
            [marzoloco.wagering.player :as p]))

;; The command handler:
;; * builds the aggregate root that the command will be executed against
;; * gathers all other dependencies needed to execute the command
;; * executes the command
;; * persists the resulting events

(defn handle-command
  [event-store {:keys [player-id] :as cmd}]
  (let [all-events (es/get-all-events event-store)
        player-events (filter #(= (:player-id %) player-id) all-events)
        initial-player (p/->Player player-id 0 #{})
        player-aggregate (reduce p/apply-event initial-player player-events)
        new-events (p/execute-command player-aggregate cmd)]
    (es/append-events event-store new-events)
    new-events))
