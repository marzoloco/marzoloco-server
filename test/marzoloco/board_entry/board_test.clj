(ns marzoloco.board-entry.board-test
  (:require [clojure.test :refer :all]
            [marzoloco.board-entry.board :refer :all]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(defn uuid [] (java.util.UUID/randomUUID))


(deftest apply-GamePosted-event
  (let [board-id (uuid) game-id (uuid) team-a-name "Butler" team-b-name "Syracuse"
        initial-board (map->Board {:board-id board-id
                                   :games    {}})
        game-posted-event {:event-type  :game-posted
                           :board-id    board-id
                           :game-id     game-id
                           :team-a-name team-a-name
                           :team-b-name team-b-name}
        expected-game (map->Game {:game-id     game-id
                                  :team-a-name team-a-name
                                  :team-b-name team-b-name})
        expected-board (map->Board {:board-id board-id
                                    :games    {game-id expected-game}})
        actual-board (apply-event initial-board game-posted-event)]
    (is (= expected-board actual-board))))

(deftest apply-BetPosted-event
  (let [board-id (uuid) game-id (uuid) bet-id (uuid)
        favorite :team-a
        spread 13
        initial-board (map->Board {:board-id board-id
                                   :games    {game-id {:game-id game-id
                                                       :bets    {}}}})
        bet-posted-event {:event-type :bet-posted
                          :board-id   board-id
                          :game-id    game-id
                          :bet        {:bet-id   bet-id
                                       :bet-type :spread-bet
                                       :favorite favorite
                                       :spread   spread}}
        expected-bet (map->SpreadBet {:bet-id   bet-id
                                      :bet-type :spread-bet
                                      :favorite favorite
                                      :spread   spread})
        expected-board (map->Board {:board-id board-id
                                    :games    {game-id {:game-id game-id
                                                        :bets    {bet-id expected-bet}}}})
        actual-board (apply-event initial-board bet-posted-event)]
    (is (= expected-board actual-board))))

(deftest apply-SideWonLostPushed-event
  (let [board-id (uuid) game-id (uuid) bet-id (uuid) other-bet-id (uuid)
        initial-board (map->Board {:board-id board-id
                                   :games    {game-id {:game-id game-id
                                                       :bets    {;; I'm wondering when this becomes a bad idea.
                                                                 ;; this event application shouldn't care about
                                                                 ;; the contents of the bet, so is it ok, or perhaps
                                                                 ;; better to include the content?
                                                                 other-bet-id {:bet-id other-bet-id}
                                                                 bet-id       {:bet-id bet-id}}}}})
        expected-board (map->Board {:board-id board-id
                                    :games    {game-id {:game-id game-id
                                                        :bets    {other-bet-id {:bet-id other-bet-id}}}}})]
    (testing "SideWon event removes bet from game"
      (let [event {:event-type :side-won
                   :board-id   board-id
                   :game-id    game-id
                   :bet-id     bet-id
                   :side       :favorite}
            actual-board (apply-event initial-board event)]
        (is (= expected-board actual-board))))
    (testing "SideLost event removes bet from game"
      (let [event {:event-type :side-lost
                   :board-id   board-id
                   :game-id    game-id
                   :bet-id     bet-id
                   :side       :favorite}
            actual-board (apply-event initial-board event)]
        (is (= expected-board actual-board))))
    (testing "SidePushed event removes bet from game"
      (let [event {:event-type :side-pushed
                   :board-id   board-id
                   :game-id    game-id
                   :bet-id     bet-id
                   :side       :favorite}
            actual-board (apply-event initial-board event)]
        (is (= expected-board actual-board))))))


(deftest execute-PostGame-command
  (let [board-id (uuid) game-id (uuid)
        team-a-name "Butler"
        team-b-name "Syracuse"
        board (map->Board {:board-id board-id
                           :games    {}})]
    (testing "PostGame -> GamePosted"
      (let [postGame-cmd {:command-type :post-game
                          :board-id     board-id
                          :game-id      game-id
                          :team-a-name  team-a-name
                          :team-b-name  team-b-name}
            expected-events [{:event-type  :game-posted
                              :board-id    board-id
                              :game-id     game-id
                              :team-a-name team-a-name
                              :team-b-name team-b-name}]
            actual-events (execute-command board postGame-cmd)]
        (is (= expected-events actual-events))))))

(deftest execute-PostBet-command
  (let [board-id (uuid) game-id (uuid) bet-id (uuid)
        board (map->Board {:board-id board-id
                           :games    {game-id {:game-id game-id
                                               :bets    {}}}})]
    (testing "PostBet,SpreadBet -> BetPosted"
      (let [bet-type :spread-bet
            favorite :team-a
            spread 13
            postBet-cmd {:command-type :post-bet
                         :board-id     board-id
                         :game-id      game-id
                         :bet          {:bet-id   bet-id
                                        :bet-type bet-type
                                        :favorite favorite
                                        :spread   spread}}
            expected-events [{:event-type :bet-posted
                              :board-id   board-id
                              :game-id    game-id
                              :bet        {:bet-id   bet-id
                                           :bet-type bet-type
                                           :favorite favorite
                                           :spread   spread}}]
            actual-events (execute-command board postBet-cmd)]
        (is (= expected-events actual-events))))
    (testing "PostBet,TotalBet -> BetPosted"
      (let [bet-type :total-bet
            over-under 13
            postBet-cmd {:command-type :post-bet
                         :board-id     board-id
                         :game-id      game-id
                         :bet          {:bet-id     bet-id
                                        :bet-type   bet-type
                                        :over-under over-under}}
            expected-events [{:event-type :bet-posted
                              :board-id   board-id
                              :game-id    game-id
                              :bet        {:bet-id     bet-id
                                           :bet-type   bet-type
                                           :over-under over-under}}]
            actual-events (execute-command board postBet-cmd)]
        (is (= expected-events actual-events))))
    (testing "PostBet,PropBet -> BetPosted"
      (let [bet-type :prop-bet
            over-under 13
            postBet-cmd {:command-type :post-bet
                         :board-id     board-id
                         :game-id      game-id
                         :bet          {:bet-id     bet-id
                                        :bet-type   bet-type
                                        :over-under over-under}}
            expected-events [{:event-type :bet-posted
                              :board-id   board-id
                              :game-id    game-id
                              :bet        {:bet-id     bet-id
                                           :bet-type   bet-type
                                           :over-under over-under}}]
            actual-events (execute-command board postBet-cmd)]
        (is (= expected-events actual-events))))))

(deftest execute-DeclareWinners-command
  (let [board-id (uuid) game-id (uuid)
        bet-id (uuid) favorite :team-a spread 5
        team-a-points 93 team-b-points 87
        board (map->Board {:board-id board-id
                           :games    {game-id {:game-id game-id
                                               :bets    {bet-id {:bet-id   bet-id
                                                                 :bet-type :spread-bet
                                                                 :favorite favorite
                                                                 :spread   spread}}}}})
        declareWinners-cmd {:command-type  :declare-winners
                            :board-id      board-id
                            :game-id       game-id
                            :team-a-points team-a-points
                            :team-b-points team-b-points}
        expected-events [{:event-type :side-won
                          :board-id   board-id
                          :game-id    game-id
                          :bet-id     bet-id
                          :side       :favorite}
                         {:event-type :side-lost
                          :board-id   board-id
                          :game-id    game-id
                          :bet-id     bet-id
                          :side       :underdog}]
        actual-events (execute-command board declareWinners-cmd)]
    (is (= expected-events actual-events))
    (testing "multiple bets of different kinds")))
