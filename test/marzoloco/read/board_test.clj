(ns marzoloco.read.board-test
  (:require [clojure.test :refer :all]
            [marzoloco.read.board :refer :all]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(defn uuid [] (java.util.UUID/randomUUID))


(deftest board-readmodel-applies-all-events
  (let [board-id (uuid)

        g1-id (uuid) g1-team-a-name "asdf" g1-team-b-name "fdsa"
        g1-events (->> [{:event-type :game-posted :team-a-name g1-team-a-name :team-b-name g1-team-b-name}]
                       (map #(assoc % :board-id board-id :game-id g1-id)))

        g2-id (uuid) g2-team-a-name "sdfg" g2-team-b-name "gfds"
        g2-b1-id (uuid) g2-b1-favorite :team-a g2-b1-spread 5.5
        g2-b2-id (uuid) g2-b2-over-under 6.5
        g2-b3-id (uuid) g2-b3-over-under 160.5
        g2-events (->> [{:event-type :game-posted :team-a-name g2-team-a-name :team-b-name g2-team-b-name}
                        {:event-type :spread-bet-posted :bet-id g2-b1-id :favorite g2-b1-favorite :spread g2-b1-spread}
                        {:event-type :total-bet-posted :bet-id g2-b2-id :over-under g2-b2-over-under}
                        {:event-type :prop-bet-posted :bet-id g2-b3-id :over-under g2-b3-over-under}]
                       (map #(assoc % :board-id board-id :game-id g2-id)))

        g3-id (uuid) g3-team-a-name "dfgh" g3-team-b-name "hgfd"
        g3-b1-id (uuid) g3-b1-favorite :team-b g3-b1-spread 5.5
        g3-team-a-points 80 g3-team-b-points 85
        g3-events (->> [{:event-type :game-posted :team-a-name g3-team-a-name :team-b-name g3-team-b-name}
                        {:event-type :spread-bet-posted :bet-id g3-b1-id :favorite g3-b1-favorite :spread g3-b1-spread}
                        {:event-type :game-results-posted :team-a-points g3-team-a-points :team-b-points g3-team-b-points}
                        {:event-type :side-won :bet-id g3-b1-id :side :underdog}
                        {:event-type :side-lost :bet-id g3-b1-id :side :favorite}]
                       (map #(assoc % :board-id board-id :game-id g3-id)))

        events (concat g1-events g2-events g3-events)
        ;; todo: interleave the events from each game randomly

        initial-board {:board-id board-id
                       :games    {}}
        ;; this goes away when Board creation events get figured out

        expected-board {:board-id board-id
                        :games    {g1-id {:game-id g1-id :team-a-name g1-team-a-name :team-b-name g1-team-b-name
                                          :status  :posted}
                                   g2-id {:game-id g2-id :team-a-name g2-team-a-name :team-b-name g2-team-b-name
                                          :status  :posted
                                          :bets    {g2-b1-id {:bet-id   g2-b1-id :bet-type :spread-bet
                                                              :favorite g2-b1-favorite :spread g2-b1-spread}
                                                    g2-b2-id {:bet-id     g2-b2-id :bet-type :total-bet
                                                              :over-under g2-b2-over-under}
                                                    g2-b3-id {:bet-id     g2-b3-id :bet-type :prop-bet
                                                              :over-under g2-b3-over-under}}}
                                   g3-id {:game-id       g3-id :team-a-name g3-team-a-name :team-b-name g3-team-b-name
                                          :status        :completed
                                          :bets          {g3-b1-id {:bet-id   g3-b1-id :bet-type :spread-bet
                                                                    :favorite g3-b1-favorite :spread g3-b1-spread}}
                                          :team-a-points g3-team-a-points :team-b-points g3-team-b-points}}}

        actual-board (reduce apply-event initial-board events)]
    (is (= expected-board actual-board))))
