(ns marzoloco.bettor-test
  (:require [clojure.test :refer :all]
            [marzoloco.aggregates.bettor :refer :all]))

(deftest apply-funds-deposited-event
  (let [bettor-id "betty"
        starting-bankroll 0.0M
        deposited-amount 200.0M
        expected-bankroll (+ starting-bankroll deposited-amount)
        initial-bettor (map->Bettor {:bettor-id bettor-id
                                     :bankroll  starting-bankroll})
        funds-deposited-event {:event-type :funds-deposited
                               :bettor-id  bettor-id
                               :amount     deposited-amount}
        expected-bettor (map->Bettor {:bettor-id bettor-id
                                      :bankroll  expected-bankroll})
        actual-bettor (apply-event initial-bettor funds-deposited-event)]
    (is (= expected-bettor actual-bettor))))

(deftest apply-bet-taken-event
  (let [bettor-id "betty"
        starting-bankroll 200.0M
        bet-amount 50.0M
        expected-bankroll (- starting-bankroll bet-amount)
        initial-bettor (map->Bettor {:bettor-id bettor-id
                                     :bankroll  starting-bankroll})
        bet-taken-event {:event-type :bet-taken
                         :bettor-id  bettor-id
                         :amount     bet-amount}
        expected-bettor (map->Bettor {:bettor-id bettor-id
                                      :bankroll  expected-bankroll})
        actual-bettor (apply-event initial-bettor bet-taken-event)]
    (is (= expected-bettor actual-bettor))))

(deftest apply-winnings-earned-event
  (let [bettor-id "betty"
        initial-winnings 100.0M
        earned-amount 50.0M
        expected-winnings (+ initial-winnings earned-amount)
        initial-bettor (map->Bettor {:bettor-id bettor-id
                                     :winnings  initial-winnings})
        winnings-earned-event {:event-type :winnings-earned
                               :bettor-id  bettor-id
                               :amount     earned-amount}
        expected-bettor (map->Bettor {:bettor-id bettor-id
                                      :winnings  expected-winnings})
        actual-bettor (apply-event initial-bettor winnings-earned-event)]
    (is (= expected-bettor actual-bettor))))

