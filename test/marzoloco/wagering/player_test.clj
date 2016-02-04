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
        other-wager (map->Wager {:wager-id (uuid) :amount 12.34M :locked? false})
        initial-open-wagers #{other-wager}
        placed-wager (map->Wager {:wager-id (uuid) :amount 23.45M :locked? false})
        expected-bankroll (- initial-bankroll (:amount placed-wager))
        expected-open-wagers #{placed-wager other-wager}
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers initial-open-wagers
                                     :bankroll    initial-bankroll})
        wager-placed-event (e/map->WagerPlaced {:player-id player-id
                                                :wager-id  (:wager-id placed-wager)
                                                :amount    (:amount placed-wager)})
        expected-player (map->Player {:player-id   player-id
                                      :bankroll    expected-bankroll
                                      :open-wagers expected-open-wagers})
        actual-player (apply-event initial-player wager-placed-event)]
    (is (= expected-player actual-player))))

(deftest apply-WagerWithdrawnCancelled-event
  (let [player-id (uuid)
        initial-bankroll 150.0M
        wager (map->Wager {:wager-id (uuid) :amount 50.0M :locked? false})
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
      (let [wager-withdrawn-event (e/map->WagerWithdrawn {:player-id player-id
                                                          :wager-id  (:wager-id wager)})
            actual-player (apply-event initial-player wager-withdrawn-event)]
        (is (= expected-player actual-player))))
    (testing "WagerCancelled increases bankroll and removes wager from open-wagers"
      (let [wager-cancelled-event (e/map->WagerCancelled {:player-id player-id
                                                          :wager-id  (:wager-id wager)})
            actual-player (apply-event initial-player wager-cancelled-event)]
        (is (= expected-player actual-player))))))

(deftest apply-WagerLocked-event
  (let [player-id (uuid)
        wager (map->Wager {:wager-id (uuid) :amount 50.0M :locked? false})
        wager-id (:wager-id wager)
        other-wager (map->Wager {:wager-id (uuid) :amount 100.0M :locked? false})
        initial-open-wagers #{wager other-wager}
        expected-wager (assoc wager :locked? true)
        expected-open-wagers #{expected-wager other-wager}
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers initial-open-wagers})
        wager-locked-event (e/map->WagerLocked {:player-id player-id
                                                :wager-id  wager-id})
        expected-player (map->Player {:player-id   player-id
                                      :open-wagers expected-open-wagers})
        actual-player (apply-event initial-player wager-locked-event)]
    (is (= expected-player actual-player))))

(deftest apply-WagerWonPushedLost-event
  (let [player-id (uuid)
        wager (map->Wager {:wager-id (uuid) :amount 23.45M :locked? true})
        wager-id (:wager-id wager)
        other-wager (map->Wager {:wager-id (uuid) :amount 12.34M :locked? false})
        initial-open-wagers #{other-wager wager}
        expected-open-wagers #{other-wager}
        initial-player (map->Player {:player-id   player-id
                                     :open-wagers initial-open-wagers})
        expected-player (map->Player {:player-id   player-id
                                      :open-wagers expected-open-wagers})]
    (testing "WagerWon event removes wager from open-wagers"
      (let [wager-won-event (e/map->WagerWon {:player-id player-id
                                              :wager-id  wager-id})
            actual-player (apply-event initial-player wager-won-event)]
        (is (= expected-player actual-player))))
    (testing "WagerPushed event removes wager from open-wagers"
      (let [wager-pushed-event (e/map->WagerPushed {:player-id player-id
                                                    :wager-id  wager-id})
            actual-player (apply-event initial-player wager-pushed-event)]
        (is (= expected-player actual-player))))
    (testing "WagerLost event removes wager from open-wagers"
      (let [wager-lost-event (e/map->WagerLost {:player-id player-id
                                                :wager-id  wager-id})
            actual-player (apply-event initial-player wager-lost-event)]
        (is (= expected-player actual-player))))))

(deftest apply-WinningsEarned-event
  (let [player-id (uuid)
        earned-amount 50.0M
        initial-player (map->Player {:player-id player-id})
        winnings-earned-event (e/map->WinningsEarned {:player-id player-id
                                                      :amount    earned-amount})
        expected-player (map->Player {:player-id player-id})
        actual-player (apply-event initial-player winnings-earned-event)]
    (is (= expected-player actual-player))))

(deftest execute-PlaceWager-command
  ; build command
  ; build aggregate from events
  ; execute command, passing in the aggregate
  ; assert that the events are correct
  nil)