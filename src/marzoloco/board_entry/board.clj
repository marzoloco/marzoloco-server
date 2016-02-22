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
                 team-a-name
                 team-b-name
                 bets])

(defrecord SpreadBet [bet-id
                      bet-type
                      favorite
                      spread])

(defrecord TotalBet [bet-id
                     bet-type
                     over-under])

(defrecord PropBet [bet-id
                    bet-type
                    over-under])


(defn dispatch-apply-event [board event] (:event-type event))

(defmulti apply-event #'dispatch-apply-event)

(s/defmethod apply-event :game-posted
  [board :- Board
   {:keys [game-id team-a-name team-b-name] :as event} :- e/GamePosted]
  (let [game (map->Game {:game-id     game-id
                         :team-a-name team-a-name
                         :team-b-name team-b-name})]
    (-> board
        (update-in [:games] assoc :game-id game))))

(s/defmethod apply-event :bet-posted
  [board :- Board
   {:keys [game-id bet] :as event} :- e/BetPosted]
  ;; TODO return BetPostedForUnknownGame event
  (let [bet-id (:bet-id bet)
        bet-type (:bet-type bet)
        bet (merge (case bet-type
                     :spread-bet (map->SpreadBet {:favorite (:favorite bet)
                                                  :spread   (:spread bet)})
                     :total-bet (map->TotalBet {:over-under (:over-under bet)})
                     :prop-bet (map->PropBet {:over-under (:over-under bet)}))
                   {:bet-id   bet-id
                    :bet-type bet-type})]
    (->> board
         (transform [:games (keypath game-id) :bets] #(assoc % bet-id bet)))))


(defn dispatch-execute-command [board command] (:command-type command))

(defmulti execute-command #'dispatch-execute-command)

(s/defmethod execute-command :post-game
  [{:keys [board-id] :as board} :- Board
   {:keys [game-id team-a-name team-b-name] :as command} :- c/PostGame]
  [{:event-type  :game-posted
    :board-id    board-id
    :game-id     game-id
    :team-a-name team-a-name
    :team-b-name team-b-name}])

(s/defmethod execute-command :post-bet
  [{:keys [board-id] :as board} :- Board
   {:keys [game-id bet] :as command} :- c/PostBet]
  (let [{:keys [bet-id bet-type]} bet]
    [{:event-type :bet-posted
      :board-id   board-id
      :game-id    game-id
      :bet        (merge {:bet-id   bet-id
                          :bet-type bet-type}
                         (case bet-type
                           :spread-bet {:favorite (:favorite bet)
                                        :spread   (:spread bet)}
                           :total-bet {:over-under (:over-under bet)}
                           :prop-bet {:over-under (:over-under bet)}))}]))
