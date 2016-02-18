(ns marzoloco.board-entry.events
  (:require [schema.core :as s]))

(s/defschema GamePosted {:event-type (s/eq :game-posted)
                         :board-id   s/Uuid
                         :game-id    s/Uuid})
