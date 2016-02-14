(ns marzoloco.read.players-test
  (:require [clojure.test :refer :all]
            [marzoloco.read.players :refer :all]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(defn uuid [] (java.util.UUID/randomUUID))

(deftest players-readmodel-applies-all-events
  (let [p1 (uuid) p1-points 200 p2 (uuid) p2-points 200 p3 (uuid) p3-points 200
        odds 2
        w1 (uuid) w1-amount 10
        w2 (uuid) w2-amount 20
        w3 (uuid) w3-amount 30
        w4 (uuid) w4-amount 40
        w5 (uuid) w5-amount 50
        w6 (uuid) w6-amount 60
        w7 (uuid) w7-amount 70 w7-winnings (* w7-amount odds)
        w8 (uuid) w8-amount 80 w8-winnings w8-amount
        w9 (uuid) w9-amount 90
        w10 (uuid)

        events [{:event-type :points-deposited :player-id p1 :amount p1-points}
                {:event-type :points-deposited :player-id p2 :amount p2-points}
                {:event-type :points-deposited :player-id p3 :amount p3-points}

                {:event-type :wager-placed :player-id p1 :wager-id w1 :amount w1-amount :odds odds}

                {:event-type :wager-placed :player-id p2 :wager-id w2 :amount w2-amount :odds odds}
                {:event-type :wager-withdrawn :player-id p2 :wager-id w2}

                {:event-type :wager-placed :player-id p3 :wager-id w3 :amount w3-amount :odds odds}
                {:event-type :wager-cancelled :player-id p3 :wager-id w3}

                {:event-type :wager-placed :player-id p1 :wager-id w4 :amount w4-amount :odds odds}
                {:event-type :wager-locked :player-id p1 :wager-id w4}

                {:event-type :wager-placed :player-id p2 :wager-id w5 :amount w5-amount :odds odds}
                {:event-type :wager-locked :player-id p2 :wager-id w5}
                {:event-type :locked-wager-withdraw-attempted :player-id p1 :wager-id w5}

                {:event-type :wager-placed :player-id p3 :wager-id w6 :amount w6-amount :odds odds}
                {:event-type :wager-locked :player-id p3 :wager-id w6}
                {:event-type :wager-cancelled :player-id p3 :wager-id w6}

                {:event-type :wager-placed :player-id p1 :wager-id w7 :amount w7-amount :odds odds}
                {:event-type :wager-won :player-id p1 :wager-id w7}
                {:event-type :winnings-earned :player-id p1 :amount w7-winnings}

                {:event-type :wager-placed :player-id p2 :wager-id w8 :amount w8-amount :odds odds}
                {:event-type :wager-pushed :player-id p2 :wager-id w8}
                {:event-type :winnings-earned :player-id p2 :amount w8-winnings}

                {:event-type :wager-placed :player-id p3 :wager-id w9 :amount w9-amount :odds odds}
                {:event-type :wager-lost :player-id p3 :wager-id w9}

                {:event-type :overdraw-attempted :player-id p1 :wager-id w10}]

        expected-players {p1 {:player-id p1 :bankroll (- p1-points w1-amount w4-amount w7-amount)
                              :winnings  w7-winnings
                              :wagers    #{{:wager-id w1 :amount w1-amount :odds odds :status :placed}
                                           {:wager-id w4 :amount w4-amount :odds odds :status :locked}
                                           {:wager-id w7 :amount w7-amount :odds odds :status :won}}}
                          p2 {:player-id p2 :bankroll (- p1-points w5-amount w8-amount)
                              :winnings  w8-winnings
                              :wagers    #{{:wager-id w2 :amount w2-amount :odds odds :status :withdrawn}
                                           {:wager-id w5 :amount w5-amount :odds odds :status :locked}
                                           {:wager-id w8 :amount w8-amount :odds odds :status :pushed}}}
                          p3 {:player-id p3 :bankroll (- p1-points w9-amount)
                              :winnings  0
                              :wagers    #{{:wager-id w3 :amount w3-amount :odds odds :status :cancelled}
                                           {:wager-id w6 :amount w6-amount :odds odds :status :cancelled}
                                           {:wager-id w9 :amount w9-amount :odds odds :status :lost}}}}

        actual-players (reduce apply-event {} events)]
    (is (= expected-players actual-players))))
