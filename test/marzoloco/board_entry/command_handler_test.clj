(ns marzoloco.board-entry.command-handler-test
  (:require [clojure.test :refer :all]
            [marzoloco.board-entry.command-handler :refer :all]
            [marzoloco.board-entry.board :as b]
            [marzoloco.event-store :as es]
            [schema.test]
            [marzoloco.board-entry.commands :as c]))

(use-fixtures :once schema.test/validate-schemas)

(defn uuid [] (java.util.UUID/randomUUID))


(deftest handle-PostGame-command
  (let [event-store (es/make-in-memory-event-store)
        board-id (uuid)
        game-id (uuid)
        cmd {:command-type :post-game
             :board-id     board-id
             :game-id      game-id}
        expected-stored-events [{:event-type :game-posted
                                 :board-id   board-id
                                 :game-id    game-id}]]
    (handle-command event-store cmd)
    (is (= expected-stored-events (es/get-all-events event-store)))))
