(ns marzoloco.board-entry.board
  (:require [schema.core :as s]
            [com.rpl.specter :refer :all]
            [marzoloco.board-entry.events :as e]
            [marzoloco.board-entry.commands :as c]))

;;
;; The Board aggregate root:
;; * ensures no duplicate games or bets are added to the board
;; * resolves which side wins a particular bet
;; *
;;

(defrecord Board [board-id
                  games])

(defrecord Game [game-id
                 bets])

(defrecord Bet [bet-id])


(defn dispatch-apply-event [board event] (:event-type event))

(defmulti apply-event #'dispatch-apply-event)

(s/defmethod apply-event :game-posted
  [board :- Board
   {:keys [game-id] :as event} :- e/GamePosted]
  (let [game (map->Game {:game-id game-id})]
    (-> board
        (update-in [:games] conj game))))


(defn dispatch-execute-command [board command] (:command-type command))

(defmulti execute-command #'dispatch-execute-command)

(s/defmethod execute-command :post-game
  [{:keys [board-id] :as board} :- Board
   {:keys [game-id] :as command} :- c/PostGame]
  [{:event-type :game-posted
    :board-id   board-id
    :game-id    game-id}])
