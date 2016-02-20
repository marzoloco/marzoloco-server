(ns marzoloco.board-entry.commands
  (:require [schema.core :as s]))

(s/defschema PostGame {:command-type (s/eq :post-game)
                       :board-id     s/Uuid
                       :game-id      s/Uuid
                       :team-a-name  s/Str
                       :team-b-name  s/Str})


