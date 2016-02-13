(ns marzoloco.event-store)

(defprotocol EventStore
  (initialize [_])
  (get-all-events [_])
  (append-events [_ events]))

(defn make-in-memory-event-store
  []
  (let [event-store
        (let [stored-events (atom [])]
          (reify EventStore
            (initialize [_]
              (reset! stored-events []))
            (get-all-events [_]
              @stored-events)
            (append-events [_ events]
              (doseq [event events]
                (swap! stored-events #(conj % event))))))]
    event-store))
