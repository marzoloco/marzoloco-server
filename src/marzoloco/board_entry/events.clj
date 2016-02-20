(ns marzoloco.board-entry.events
  (:require [schema.core :as s]))

(s/defschema GamePosted {:event-type  (s/eq :game-posted)
                         :board-id    s/Uuid
                         :game-id     s/Uuid
                         :team-a-name s/Str
                         :team-b-name s/Str})
