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

        ;; this goes away when Board creation events get figured out
        initial-board {:board-id board-id
                       :games    {}}

        events [{:event-type  :game-posted :board-id board-id :game-id g1
                 :team-a-name g1-team-a-name :team-b-name g1-team-b-name}
                {:event-type :spread-bet-posted :board-id board-id :game-id g1
                 :bet-id     b1 :favorite b1-favorite :spread b1-spread}]

        expected-board {:board-id board-id
                        :games    {g1 {:game-id g1 :team-a-name g1-team-a-name :team-b-name g1-team-b-name
                                       :bets    {b1 {:bet-id   b1 :bet-type :spread-bet
                                                     :favorite b1-favorite :spread b1-spread}}}}}
        actual-board (reduce apply-event initial-board events)]
    (is (= expected-board actual-board))))
