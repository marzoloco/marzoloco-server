(ns marzoloco.board-entry.command-handler
  (:require [marzoloco.event-store :as es]
            [marzoloco.board-entry.board :as b]))

;; The command handler:
;; * builds the aggregate root that the command will be executed against
;; * gathers all other dependencies needed to execute the command
;; * executes the command
;; * persists the resulting events

(defn handle-command
  [event-store {:keys [board-id] :as cmd}]
  (let [all-events (es/get-all-events event-store)
        board-events (filter #(= (:board-id %) board-id) all-events)
        initial-board (b/->Board board-id [])
        board-aggregate (reduce b/apply-event initial-board board-events)
        new-events (b/execute-command board-aggregate cmd)]
    (es/append-events event-store new-events)
    new-events))
