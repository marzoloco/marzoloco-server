(ns marzoloco.read.board
  (:require [schema.core :as s]
            [com.rpl.specter :refer :all]
            [marzoloco.board-entry.events :as be]
            [marzoloco.event-store :as es]))

;;
;; Consuming all Board-related events to build a read model for the Board.
;; The apply-event functions build up the Board with a map of Games,
;; keyed by the game-id which include a map of Bets, keyed by bet-id.
;;
;; The `get` functions return the Games and Bets as a vector instead of a map.
;;

(defn dispatch-apply-event [board event] (:event-type event))

(defmulti apply-event #'dispatch-apply-event)

(s/defmethod apply-event :game-posted
  [board
   {:keys [board-id game-id] :as event} :- be/GamePosted]
  (let [game (-> event
                 (select-keys [:game-id :team-a-name :team-b-name])
                 (assoc :status :posted))]
    (transform [:games] #(assoc % game-id game) board)))

(s/defmethod apply-event :spread-bet-posted
  [board
   {:keys [game-id bet-id] :as event} :- be/SpreadBetPosted]
  (let [bet (-> event
                (select-keys [:bet-id :favorite :spread])
                (assoc :bet-type :spread-bet))]
    (transform [:games (keypath game-id) :bets] #(assoc % bet-id bet) board)))

(s/defmethod apply-event :total-bet-posted
  [board
   {:keys [game-id bet-id] :as event} :- be/TotalBetPosted]
  (let [bet (-> event
                (select-keys [:bet-id :over-under])
                (assoc :bet-type :total-bet))]
    (transform [:games (keypath game-id) :bets] #(assoc % bet-id bet) board)))

(s/defmethod apply-event :prop-bet-posted
  [board
   {:keys [game-id bet-id] :as event} :- be/PropBetPosted]
  (let [bet (-> event
                (select-keys [:bet-id :over-under])
                (assoc :bet-type :prop-bet))]
    (transform [:games (keypath game-id) :bets] #(assoc % bet-id bet) board)))

(s/defmethod apply-event :game-results-posted
  [board
   {:keys [game-id team-a-points team-b-points] :- be/GameResultsPosted}]
  (->> board
       (transform [:games (keypath game-id)] #(assoc % :team-a-points team-a-points
                                                       :team-b-points team-b-points
                                                       :status :completed))))

(s/defmethod apply-event :side-won
  [board
   {:keys [game-id bet-id side] :- be/SideWon}]
  board)

(s/defmethod apply-event :side-lost
  [board
   {:keys [game-id bet-id side] :- be/SideLost}]
  board)

(defn make-board
  [events]
  (reduce apply-event {} events))

(defn get-board
  [event-store]
  (->> event-store
       es/get-all-events
       make-board))

(defn get-game
  [event-store game-id]
  (->> event-store
       es/get-all-events
       (filter #(= (:game-id %) game-id))
       make-board
       #(get % game-id)))
