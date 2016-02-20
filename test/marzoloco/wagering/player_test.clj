(ns marzoloco.wagering.player-test
  (:require [clojure.test :refer :all]
            [marzoloco.wagering.player :refer :all]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(defn uuid [] (java.util.UUID/randomUUID))


(deftest apply-PointsDeposited-event
  (let [player-id (uuid)
        initial-bankroll 0.0
        deposited-amount 200.0
        expected-bankroll (+ initial-bankroll deposited-amount)
        initial-player (map->Player {:player-id player-id
                                     :bankroll  initial-bankroll})
        points-deposited-event {:event-type :points-deposited
                                :player-id  player-id
                                :amount     deposited-amount}
        expected-player (map->Player {:player-id player-id
                                      :bankroll  expected-bankroll})
        actual-player (apply-event initial-player points-deposited-event)]
    (is (= expected-player actual-player))))

(deftest apply-WagerPlaced-event
  (let [player-id (uuid)
        initial-bankroll 200.0
        other-wager (map->Wager {:wager-id (uuid) :amount 12.34 :odds 2.0 :locked? false})
        initial-open-wagers #{other-wager}
        placed-wager (map->Wager {:wager-id (uuid) :amount 23.45 :odds 2.0 :locked? false})
        expected-bankroll (- initial-bankroll (:amount placed-wager))
        expected-open-wagers #{placed-wager other-wager}
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers initial-open-wagers
                                     :bankroll    initial-bankroll})
        wager-placed-event {:event-type :wager-placed
                            :player-id  player-id
                            :wager-id   (:wager-id placed-wager)
                            :amount     (:amount placed-wager)
                            :odds       2.0}
        expected-player (map->Player {:player-id   player-id
                                      :bankroll    expected-bankroll
                                      :open-wagers expected-open-wagers})
        actual-player (apply-event initial-player wager-placed-event)]
    (is (= expected-player actual-player))))

(deftest apply-noop-events
  (let [player-id (uuid)
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers #{(map->Wager {:wager-id (uuid)
                                                                 :amount   12.34
                                                                 :odds     2.0 :locked? false})}
                                     :bankroll    5})
        expected-player initial-player]
    (testing "applying OverdrawAttempted event is a no-op"
      (let [overdrawAttempted-event {:event-type :overdraw-attempted
                                     :player-id  player-id
                                     :wager-id   (uuid)}
            actual-player (apply-event initial-player overdrawAttempted-event)]
        (is (= expected-player actual-player))))
    (testing "applying LockedWagerWithdrawAttempted event is a no-op"
      (let [lockedWagerWithdrawAttempted-event {:event-type :locked-wager-withdraw-attempted
                                                :player-id  player-id
                                                :wager-id   (uuid)}
            actual-player (apply-event initial-player lockedWagerWithdrawAttempted-event)]
        (is (= expected-player actual-player))))))

(deftest apply-WagerWithdrawnCancelled-event
  (let [player-id (uuid)
        initial-bankroll 150.0
        wager (map->Wager {:wager-id (uuid) :amount 50.0 :locked? false})
        initial-open-wagers #{wager}
        expected-bankroll (+ initial-bankroll (:amount wager))
        expected-open-wagers #{}
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers initial-open-wagers
                                     :bankroll    initial-bankroll})
        expected-player (map->Player {:player-id   player-id
                                      :bankroll    expected-bankroll
                                      :open-wagers expected-open-wagers})]
    (testing "WagerWithdrawn increases bankroll and removes wager from open-wagers"
      (let [wager-withdrawn-event {:event-type :wager-withdrawn
                                   :player-id  player-id
                                   :wager-id   (:wager-id wager)}
            actual-player (apply-event initial-player wager-withdrawn-event)]
        (is (= expected-player actual-player))))
    (testing "WagerCancelled increases bankroll and removes wager from open-wagers"
      (let [wager-cancelled-event {:event-type :wager-cancelled
                                   :player-id  player-id
                                   :wager-id   (:wager-id wager)}
            actual-player (apply-event initial-player wager-cancelled-event)]
        (is (= expected-player actual-player))))))

(deftest apply-WagerLocked-event
  (let [player-id (uuid)
        wager (map->Wager {:wager-id (uuid) :amount 50.0 :locked? false})
        wager-id (:wager-id wager)
        other-wager (map->Wager {:wager-id (uuid) :amount 100.0 :locked? false})
        initial-open-wagers #{wager other-wager}
        expected-wager (assoc wager :locked? true)
        expected-open-wagers #{expected-wager other-wager}
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers initial-open-wagers})
        wager-locked-event {:event-type :wager-locked
                            :player-id  player-id
                            :wager-id   wager-id}
        expected-player (map->Player {:player-id   player-id
                                      :open-wagers expected-open-wagers})
        actual-player (apply-event initial-player wager-locked-event)]
    (is (= expected-player actual-player))))

(deftest apply-WagerWonPushedLost-event
  (let [player-id (uuid)
        wager (map->Wager {:wager-id (uuid) :amount 23.45 :locked? true})
        wager-id (:wager-id wager)
        other-wager (map->Wager {:wager-id (uuid) :amount 12.34 :locked? false})
        initial-open-wagers #{other-wager wager}
        expected-open-wagers #{other-wager}
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers initial-open-wagers})
        expected-player (map->Player {:player-id   player-id
                                      :open-wagers expected-open-wagers})]
    (testing "WagerWon event removes wager from open-wagers"
      (let [wager-won-event {:event-type :wager-won
                             :player-id  player-id
                             :wager-id   wager-id}
            actual-player (apply-event initial-player wager-won-event)]
        (is (= expected-player actual-player))))
    (testing "WagerPushed event removes wager from open-wagers"
      (let [wager-pushed-event {:event-type :wager-pushed
                                :player-id  player-id
                                :wager-id   wager-id}
            actual-player (apply-event initial-player wager-pushed-event)]
        (is (= expected-player actual-player))))
    (testing "WagerLost event removes wager from open-wagers"
      (let [wager-lost-event {:event-type :wager-lost
                              :player-id  player-id
                              :wager-id   wager-id}
            actual-player (apply-event initial-player wager-lost-event)]
        (is (= expected-player actual-player))))))

(deftest apply-WinningsEarned-event
  (let [player-id (uuid)
        earned-amount 50.0
        initial-player (map->Player {:player-id player-id})
        winnings-earned-event {:event-type :winnings-earned
                               :player-id  player-id
                               :amount     earned-amount}
        expected-player (map->Player {:player-id player-id})
        actual-player (apply-event initial-player winnings-earned-event)]
    (is (= expected-player actual-player))))


(deftest execute-PointsDeposited-command
  (let [player-id (uuid)
        player (map->Player {:player-id player-id})]
    (testing "DepositPoints -> PointsDeposited"
      (let [deposited-amount 200.00
            depositPoints-cmd {:command-type :deposit-points
                               :player-id    player-id
                               :amount       deposited-amount}
            expected-events [{:event-type :points-deposited
                              :player-id  player-id
                              :amount     deposited-amount}]
            actual-events (execute-command player depositPoints-cmd)]
        (is (= expected-events actual-events))))))

(deftest execute-PlaceWager-command
  (let [player-id (uuid)
        bankroll 123.45
        player (map->Player {:player-id player-id
                             :bankroll  bankroll})
        wager-id (uuid)
        odds 3.0
        base-placeWager-cmd {:command-type :place-wager
                             :player-id    player-id
                             :wager-id     wager-id
                             :game-id      (uuid)
                             :bet-id       (uuid)
                             :side         :a
                             :odds         odds}]
    (testing "PlaceWager for less than bankroll -> WagerPlaced"
      (let [wager-amount (/ bankroll 2)
            placeWager-cmd (assoc base-placeWager-cmd :amount wager-amount)
            expected-events [{:event-type :wager-placed
                              :player-id  player-id
                              :wager-id   wager-id
                              :amount     wager-amount
                              :odds       odds}]
            actual-events (execute-command player placeWager-cmd)]
        (is (= expected-events actual-events))))
    (testing "PlaceWager for entire bankroll -> WagerPlaced"
      (let [wager-amount bankroll
            placeWager-cmd (assoc base-placeWager-cmd :amount wager-amount)
            expected-events [{:event-type :wager-placed
                              :player-id  player-id
                              :wager-id   wager-id
                              :amount     wager-amount
                              :odds       odds}]
            actual-events (execute-command player placeWager-cmd)]
        (is (= expected-events actual-events))))
    (testing "PlaceWager over bankroll -> OverdrawAttempted"
      (let [wager-amount (* bankroll 2)
            placeWager-cmd (assoc base-placeWager-cmd :amount wager-amount)
            expected-events [{:event-type :overdraw-attempted
                              :player-id  player-id
                              :wager-id   wager-id}]
            actual-events (execute-command player placeWager-cmd)]
        (is (= expected-events actual-events))))))

(deftest execute-WithdrawWager-command
  (let [player-id (uuid)
        bankroll 123.45
        wager-id (uuid)
        other-wager (map->Wager {:wager-id (uuid)
                                 :amount   25.0
                                 :locked?  false})
        withdrawWager-cmd {:command-type :withdraw-wager
                           :player-id    player-id
                           :wager-id     wager-id}]
    (testing "WithdrawWager on not locked wager -> WagerWithdrawn"
      (let [wager (map->Wager {:wager-id wager-id
                               :amount   50.0
                               :locked?  false})
            player (map->Player {:player-id   player-id
                                 :bankroll    bankroll
                                 :open-wagers #{wager other-wager}})
            expected-events [{:event-type :wager-withdrawn
                              :player-id  player-id
                              :wager-id   wager-id}]
            actual-events (execute-command player withdrawWager-cmd)]
        (is (= expected-events actual-events))))
    (testing "WithdrawWager on locked wager -> LockedWagerWithdrawAttempted"
      (let [wager (map->Wager {:wager-id wager-id
                               :amount   50.0
                               :locked?  true})
            player (map->Player {:player-id   player-id
                                 :bankroll    bankroll
                                 :open-wagers #{wager other-wager}})
            expected-events [{:event-type :locked-wager-withdraw-attempted
                              :player-id  player-id
                              :wager-id   wager-id}]
            actual-events (execute-command player withdrawWager-cmd)]
        (is (= expected-events actual-events))))))

(deftest execute-CancelWager-command
  (let [player-id (uuid)
        bankroll 123.45
        wager-id (uuid)
        wager (map->Wager {:wager-id wager-id
                           :amount   50.0
                           :locked?  false})
        other-wager (map->Wager {:wager-id (uuid)
                                 :amount   25.0
                                 :locked?  false})
        player (map->Player {:player-id   player-id
                             :bankroll    bankroll
                             :open-wagers #{wager other-wager}})
        cancelWager-cmd {:command-type :cancel-wager
                         :player-id    player-id
                         :wager-id     wager-id}
        expected-events [{:event-type :wager-cancelled
                          :player-id  player-id
                          :wager-id   wager-id}]
        actual-events (execute-command player cancelWager-cmd)]
    (is (= expected-events actual-events))))

(deftest execute-LockWager-command
  (let [player-id (uuid)
        wager-id (uuid)
        wager (map->Wager {:wager-id wager-id
                           :amount   50.0
                           :locked?  false})
        other-wager (map->Wager {:wager-id (uuid)
                                 :amount   25.0
                                 :locked?  false})
        player (map->Player {:player-id   player-id
                             :open-wagers #{wager other-wager}})
        lockWager-cmd {:command-type :lock-wager
                       :player-id    player-id
                       :wager-id     wager-id}
        expected-events [{:event-type :wager-locked
                          :player-id  player-id
                          :wager-id   wager-id}]
        actual-events (execute-command player lockWager-cmd)]
    (is (= expected-events actual-events))))

(deftest execute-CloseWonPushedLostWager-command
  (let [player-id (uuid)
        wager-id (uuid)
        wager-amount 50.0
        odds 3.0
        expected-winnings 150.0
        expected-push-winnings 50.0
        wager (map->Wager {:wager-id wager-id
                           :amount   wager-amount
                           :odds     odds})
        player (map->Player {:player-id   player-id
                             :open-wagers #{wager}})]
    (testing "CloseWonWager -> WagerWon and WinningsEarned"
      (let [closeWonWager-cmd {:command-type :close-won-wager
                               :player-id    player-id
                               :wager-id     wager-id}
            expected-events [{:event-type :wager-won
                              :player-id  player-id
                              :wager-id   wager-id}
                             {:event-type :winnings-earned
                              :player-id  player-id
                              :amount     expected-winnings}]
            actual-events (execute-command player closeWonWager-cmd)]
        (is (= expected-events actual-events))))
    (testing "ClosePushedWager -> WagerPushed and WinningsEarned"
      (let [closePushedWager-cmd {:command-type :close-pushed-wager
                                  :player-id    player-id
                                  :wager-id     wager-id}
            expected-events [{:event-type :wager-pushed
                              :player-id  player-id
                              :wager-id   wager-id}
                             {:event-type :winnings-earned
                              :player-id  player-id
                              :amount     expected-push-winnings}]
            actual-events (execute-command player closePushedWager-cmd)]
        (is (= expected-events actual-events))))
    (testing "CloseLostWager -> WagerLost"
      (let [closeLostWager-cmd {:command-type :close-lost-wager
                                :player-id    player-id
                                :wager-id     wager-id}
            expected-events [{:event-type :wager-lost
                              :player-id  player-id
                              :wager-id   wager-id}]
            actual-events (execute-command player closeLostWager-cmd)]
        (is (= expected-events actual-events))))))
