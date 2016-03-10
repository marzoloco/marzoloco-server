(ns marzoloco.wagering.command-handler-test
  (:require [clojure.test :refer :all]
            [marzoloco.wagering.command-handler :refer :all]
            [marzoloco.wagering.player :as p]
            [marzoloco.event-store :as es]
            [schema.test]
            [marzoloco.wagering.commands :as c]))

(use-fixtures :once schema.test/validate-schemas)

(defn uuid [] (java.util.UUID/randomUUID))


(defn make-placeWager-cmd
  [player-id wager-id amount]
  {:command-type :place-wager
   :player-id    player-id
   :game-id      (uuid)
   :bet-id       (uuid)
   :wager-id     wager-id
   :amount       amount
   :side         :a
   :odds         2})


(deftest handle-DepositPoints-command
  (let [event-store (es/make-in-memory-event-store)
        player-id (uuid)
        amount 23.4
        cmd {:command-type :deposit-points
             :player-id    player-id
             :amount       amount}
        expected-stored-events [{:event-type :points-deposited
                                 :player-id  player-id
                                 :amount     amount}]]
    (handle-command event-store cmd)
    (is (= expected-stored-events (es/get-all-events event-store)))))

(deftest wagering-more-than-deposited->overdraw-event
  (let [event-store (es/make-in-memory-event-store)
        player-id (uuid)
        deposit-amount 20
        wager-id (uuid)
        wager-amount 30
        depositPoints-cmd {:command-type :deposit-points
                           :player-id    player-id
                           :amount       deposit-amount}
        placeWager-cmd (make-placeWager-cmd player-id wager-id wager-amount)
        expected-last-event {:event-type :overdraw-attempted
                             :player-id  player-id
                             :wager-id   wager-id}]
    (handle-command event-store depositPoints-cmd)
    (handle-command event-store placeWager-cmd)
    (is (= expected-last-event (last (es/get-all-events event-store))))))
