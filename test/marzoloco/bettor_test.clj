(ns marzoloco.bettor-test
  (:require [clojure.test :refer :all]
            [marzoloco.aggregates.bettor :refer :all]))

(deftest apply-funds-deposited-event
  (let [bettor-id "betty"
        starting-bankroll 0.0M
        deposited-amount 200.0M
        initial-bettor (map->Bettor {:bettor-id bettor-id
                                     :bankroll  starting-bankroll})
        funds-deposited-event {:event-type :funds-deposited
                               :bettor-id  bettor-id
                               :amount     deposited-amount}
        expected-bettor (map->Bettor {:bettor-id bettor-id
                                      :bankroll  (+ starting-bankroll deposited-amount)})
        actual-bettor (apply-event initial-bettor funds-deposited-event)]
    (is (= expected-bettor actual-bettor))))
