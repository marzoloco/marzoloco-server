(ns marzoloco.board-entry.events
  (:require [schema.core :as s]))

(s/defschema GamePosted {:event-type  (s/eq :game-posted)
                         :board-id    s/Uuid
                         :game-id     s/Uuid
                         :team-a-name s/Str
                         :team-b-name s/Str})

(s/defschema SpreadBetPosted {:event-type (s/eq :spread-bet-posted)
                              :board-id   s/Uuid
                              :game-id    s/Uuid
                              :bet-id     s/Uuid
                              :favorite   (s/enum :team-a :team-b)
                              :spread     s/Num})

(s/defschema TotalBetPosted {:event-type (s/eq :total-bet-posted)
                             :board-id   s/Uuid
                             :game-id    s/Uuid
                             :bet-id     s/Uuid
                             :over-under s/Num})

(s/defschema PropBetPosted {:event-type (s/eq :prop-bet-posted)
                            :board-id   s/Uuid
                            :game-id    s/Uuid
                            :bet-id     s/Uuid
                            :over-under s/Num})

(s/defschema GameResultsPosted {:event-type     (s/eq :game-results-posted)
                                :board-id       s/Uuid
                                :game-id        s/Uuid
                                :team-a-points  s/Int
                                :team-b-pointes s/Int})

(def Side (s/enum :favorite :underdog :over :under))

(s/defschema SideWon {:event-type (s/eq :side-won)
                      :board-id   s/Uuid
                      :game-id    s/Uuid
                      :bet-id     s/Uuid
                      :side       Side})

(s/defschema SideLost {:event-type (s/eq :side-lost)
                       :board-id   s/Uuid
                       :game-id    s/Uuid
                       :bet-id     s/Uuid
                       :side       Side})

(s/defschema SidePushed {:event-type (s/eq :side-pushed)
                         :board-id   s/Uuid
                         :game-id    s/Uuid
                         :bet-id     s/Uuid
                         :side       Side})
