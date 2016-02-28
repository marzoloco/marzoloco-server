(ns marzoloco.read.board-test
  (:require [clojure.test :refer :all]
            [marzoloco.read.board :refer :all]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(defn uuid [] (java.util.UUID/randomUUID))

(deftest board-readmodel-applies-all-events
  (let [board-id (uuid)
        g1 (uuid) g1-team-a-name "asdf" g1-team-b-name "fdsa"
        b1 (uuid) b1-favorite :team-a b1-spread 5.5
        b2 (uuid) b2-over-under 6.5
        g2 (uuid) g2-team-a-name "sdfg" g2-team-b-name "gfds"
        b3 (uuid) b3-over-under 7.5

        ;; this goes away when Board creation events get figured out
        initial-board {:board-id board-id
                       :games    {}}

        events (map
                 #(assoc % :board-id board-id)
                 [{:event-type :game-posted :game-id g1 :team-a-name g1-team-a-name :team-b-name g1-team-b-name}
                  {:event-type :spread-bet-posted :game-id g1 :bet-id b1 :favorite b1-favorite :spread b1-spread}
                  {:event-type :total-bet-posted :game-id g1 :bet-id b2 :over-under b2-over-under}
                  {:event-type :game-posted :game-id g2 :team-a-name g2-team-a-name :team-b-name g2-team-b-name}
                  {:event-type :prop-bet-posted :game-id g2 :bet-id b3 :over-under b3-over-under}])

        expected-board {:board-id board-id
                        :games    {g1 {:game-id g1 :team-a-name g1-team-a-name :team-b-name g1-team-b-name
                                       :bets    {b1 {:bet-id   b1 :bet-type :spread-bet
                                                     :favorite b1-favorite :spread b1-spread}
                                                 b2 {:bet-id     b2 :bet-type :total-bet
                                                     :over-under b2-over-under}}}
                                   g2 {:game-id g2 :team-a-name g2-team-a-name :team-b-name g2-team-b-name
                                       :bets    {b3 {:bet-id     b3 :bet-type :prop-bet
                                                     :over-under b3-over-under}}}}}

        actual-board (reduce apply-event initial-board events)]
    (is (= expected-board actual-board))))
