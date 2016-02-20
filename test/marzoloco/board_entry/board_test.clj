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
                                    :games    {:game-id expected-game}})
        actual-board (apply-event initial-board game-posted-event)]
    (is (= expected-board actual-board))))

(deftest apply-BetPosted-event
  (let [board-id (uuid) game-id (uuid) bet-id (uuid)
        initial-board (map->Board {:board-id board-id
                                   :games    {game-id {:game-id game-id
                                                       :bets    {}}}})
        bet-posted-event {:event-type :bet-posted
                          :board-id   board-id
                          :game-id    game-id
                          :bet        {:bet-id        bet-id
                                       :bet-type      :spread-bet
                                       :favorite-side :team-a
                                       :spread        13}}
        expected-bet (map->SpreadBet {:bet-id        bet-id
                                      :bet-type      :spread-bet
                                      :favorite-side :team-a
                                      :spread        13})
        expected-board (map->Board {:board-id board-id
                                    :games    {game-id {:game-id game-id
                                                        :bets    {bet-id expected-bet}}}})
        actual-board (apply-event initial-board bet-posted-event)]
    (is (= expected-board actual-board))))


(deftest execute-PostGame-command
  (let [board-id (uuid)
        game-id (uuid)
        team-a-name "Butler"
        team-b-name "Syracuse"
        board (map->Board {:board-id board-id
                           :games    []})]
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
