(ns marzoloco.bettor-test
  (:require [clojure.test :refer :all]
            [marzoloco.aggregates.bettor :refer :all]))

(deftest apply-bettor-funds-deposited-event
  (let [bettor-id "betty"
        starting-bankroll 0.0M
        deposited-amount 200.0M
        initial-agg (map->Bettor {:bettor-id bettor-id
                                  :bankroll  starting-bankroll})
        bfd-event {:event-type :bettor-funds-deposited
                   :bettor-id  bettor-id
                   :amount     deposited-amount}
        expected-agg (map->Bettor {:bettor-id bettor-id
                                   :bankroll  (+ starting-bankroll deposited-amount)})
        actual-agg (apply-event initial-agg bfd-event)]
    (is (= expected-agg actual-agg))))
