(ns marzoloco.wagering.player-test
  (:require [clojure.test :refer :all]
            [marzoloco.wagering.player :refer :all]
            [marzoloco.wagering.events :as e]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(defn uuid [] (java.util.UUID/randomUUID))

(deftest apply-points-deposited-event
  (let [player-id "betty"
        initial-bankroll 0.0M
        deposited-amount 200.0M
        expected-bankroll (+ initial-bankroll deposited-amount)
        initial-player (map->Player {:player-id player-id
                                     :bankroll  initial-bankroll})
        points-deposited-event (e/map->PointsDeposited {:player-id player-id
                                                        :amount    deposited-amount})
        expected-player (map->Player {:player-id player-id
                                      :bankroll  expected-bankroll})
        actual-player (apply-event initial-player points-deposited-event)]
    (is (= expected-player actual-player))))

(deftest apply-wager-placed-event
  (let [player-id "betty"
        initial-bankroll 200.0M
        wager-id (uuid)
        wager-amount 50.0M
        expected-bankroll (- initial-bankroll wager-amount)
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers []
                                     :bankroll    initial-bankroll})
        wager-placed-event (e/map->WagerPlaced {:player-id player-id
                                                :wager-id  wager-id
                                                :amount    wager-amount})
        expected-player (map->Player {:player-id   player-id
                                      :bankroll    expected-bankroll
                                      :open-wagers [wager-id]})
        actual-player (apply-event initial-player wager-placed-event)]
    (is (= expected-player actual-player))))

(deftest apply-winnings-earned-event
  (let [player-id "betty"
        initial-winnings 100.0M
        earned-amount 50.0M
        expected-winnings (+ initial-winnings earned-amount)
        initial-player (map->Player {:player-id player-id
                                     :winnings  initial-winnings})
        winnings-earned-event (e/map->WinningsEarned {:player-id player-id
                                                      :amount    earned-amount})
        expected-player (map->Player {:player-id player-id
                                      :winnings  expected-winnings})
        actual-player (apply-event initial-player winnings-earned-event)]
    (is (= expected-player actual-player))))

