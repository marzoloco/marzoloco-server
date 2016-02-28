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
        (update-in [:games] assoc game-id game))))

(s/defmethod apply-event :spread-bet-posted
  [board :- Board
   {:keys [game-id bet-id favorite spread] :as event} :- e/SpreadBetPosted]
  ;; TODO return BetPostedForUnknownGame event
  (let [bet (map->SpreadBet {:bet-id   bet-id
                             :bet-type :spread-bet
                             :favorite favorite
                             :spread   spread})]
    (->> board
         (transform [:games (keypath game-id) :bets] #(assoc % bet-id bet)))))

(s/defmethod apply-event :total-bet-posted
  [board :- Board
   {:keys [game-id bet-id over-under] :as event} :- e/TotalBetPosted]
  (let [bet (map->TotalBet {:bet-id     bet-id
                            :bet-type   :total-bet
                            :over-under over-under})]
    (->> board
         (transform [:games (keypath game-id) :bets] #(assoc % bet-id bet)))))

(s/defmethod apply-event :prop-bet-posted
  [board :- Board
   {:keys [game-id bet-id over-under] :as event} :- e/PropBetPosted]
  (let [bet (map->PropBet {:bet-id     bet-id
                           :bet-type   :prop-bet
                           :over-under over-under})]
    (->> board
         (transform [:games (keypath game-id) :bets] #(assoc % bet-id bet)))))

(defn remove-bet [game-id bet-id board]
  (->> board
       (transform [:games (keypath game-id) :bets] #(dissoc % bet-id))))

(s/defmethod apply-event :side-won
  [board :- Board
   {:keys [game-id bet-id] :as event} :- e/SideWon]
  (remove-bet game-id bet-id board))

(s/defmethod apply-event :side-lost
  [board :- Board
   {:keys [game-id bet-id] :as event} :- e/SideLost]
  (remove-bet game-id bet-id board))

(s/defmethod apply-event :side-pushed
  [board :- Board
   {:keys [game-id bet-id] :as event} :- e/SidePushed]
  (remove-bet game-id bet-id board))


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

(s/defmethod execute-command :post-spread-bet
  [{:keys [board-id] :as board} :- Board
   {:keys [game-id bet-id favorite spread] :as command} :- c/PostSpreadBet]
  [{:event-type :spread-bet-posted
    :board-id   board-id
    :game-id    game-id
    :bet-id     bet-id
    :favorite   favorite
    :spread     spread}])

(s/defmethod execute-command :post-total-bet
  [{:keys [board-id] :as board} :- Board
   {:keys [game-id bet-id over-under] :as command} :- c/PostTotalBet]
  [{:event-type :total-bet-posted
    :board-id   board-id
    :game-id    game-id
    :bet-id     bet-id
    :over-under over-under}])

(s/defmethod execute-command :post-prop-bet
  [{:keys [board-id] :as board} :- Board
   {:keys [game-id bet-id over-under] :as command} :- c/PostPropBet]
  [{:event-type :prop-bet-posted
    :board-id   board-id
    :game-id    game-id
    :bet-id     bet-id
    :over-under over-under}])

(defn determine-side-results
  [{:keys [bet-type] :as bet} team-a-points team-b-points]
  (case bet-type
    :spread-bet (let [{:keys [favorite spread]} bet
                      [favorite-points underdog-points] (if (= favorite :team-a)
                                                          [team-a-points team-b-points]
                                                          [team-b-points team-a-points])
                      cover-points (- favorite-points spread underdog-points)]

                  (cond (> cover-points 0) {:winning-sides [:favorite]
                                            :losing-sides  [:underdog]
                                            :pushed-sides  []}
                        (= cover-points 0) {:winning-sides []
                                            :losing-sides  []
                                            :pushed-sides  [:favorite :underdog]}
                        (< cover-points 0) {:winning-sides [:underdog]
                                            :losing-sides  [:favorite]
                                            :pushed-sides  []}))))

(s/defmethod execute-command :declare-winners
  [{:keys [board-id] :as board} :- Board
   {:keys [game-id team-a-points team-b-points] :as command} :- c/DeclareWinners]
  (let [[bet-id bet] (select-one [:games (keypath game-id) :bets FIRST] board)
        side-results (determine-side-results bet team-a-points team-b-points)
        side-won-events (map #(identity {:event-type :side-won
                                         :board-id   board-id
                                         :game-id    game-id
                                         :bet-id     bet-id
                                         :side       %})
                             (:winning-sides side-results))
        side-lost-events (map #(identity {:event-type :side-lost
                                          :board-id   board-id
                                          :game-id    game-id
                                          :bet-id     bet-id
                                          :side       %})
                              (:losing-sides side-results))
        side-pushed-events (map #({:event-type :side-pushed
                                   :game-id    game-id
                                   :bet-id     bet-id
                                   :side       %})
                                (:pushed-sides side-results))]
    (concat side-won-events side-lost-events side-pushed-events)))
