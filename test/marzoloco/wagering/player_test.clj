(ns marzoloco.wagering.player-test
  (:require [clojure.test :refer :all]
            [marzoloco.wagering.player :refer :all]
            [marzoloco.wagering.events :as e]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(defn uuid [] (java.util.UUID/randomUUID))

(deftest apply-PointsDeposited-event
  (let [player-id (uuid)
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

(deftest apply-WagerPlaced-event
  (let [player-id (uuid)
        initial-bankroll 200.0M
        other-wager-id (uuid)
        initial-open-wagers #{other-wager-id}
        wager-id (uuid)
        wager-amount 50.0M
        expected-bankroll (- initial-bankroll wager-amount)
        expected-open-wagers #{other-wager-id wager-id}
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers initial-open-wagers
                                     :bankroll    initial-bankroll})
        wager-placed-event (e/map->WagerPlaced {:player-id player-id
                                                :wager-id  wager-id
                                                :amount    wager-amount})
        expected-player (map->Player {:player-id   player-id
                                      :bankroll    expected-bankroll
                                      :open-wagers expected-open-wagers})
        actual-player (apply-event initial-player wager-placed-event)]
    (is (= expected-player actual-player))))

(deftest apply-WagerWon-event
  (testing "WagerWon event removes wager from open-wagers")
  (let [player-id (uuid)
        won-wager-id (uuid)
        other-wager-id (uuid)
        initial-open-wagers #{won-wager-id other-wager-id}
        expected-open-wagers #{other-wager-id}
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers initial-open-wagers})
        wager-won-event (e/map->WagerWon {:player-id player-id
                                          :wager-id  won-wager-id})
        expected-player (map->Player {:player-id   player-id
                                      :open-wagers expected-open-wagers})
        actual-player (apply-event initial-player wager-won-event)]
    (is (= expected-player actual-player))))

(deftest apply-WinningsEarned-event
  (let [player-id (uuid)
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

