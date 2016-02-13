(ns marzoloco.wagering.events
  (:require [schema.core :as s]))

(s/defschema PointsDeposited {:event-type (s/eq :points-deposited)
                              :player-id  s/Uuid
                              :amount     s/Num})

(s/defschema WagerPlaced {:event-type (s/eq :wager-placed)
                          :player-id  s/Uuid
                          :wager-id   s/Uuid
                          :amount     s/Num
                          :odds       s/Num})

(s/defschema OverdrawAttempted {:event-type (s/eq :overdraw-attempted)
                                :player-id  s/Uuid
                                :wager-id   s/Uuid})

(s/defschema WagerWithdrawn {:event-type (s/eq :wager-withdrawn)
                             :player-id  s/Uuid
                             :wager-id   s/Uuid})

(s/defschema LockedWagerWithdrawAttempted {:event-type (s/eq :locked-wager-withdraw-attempted)
                                           :player-id  s/Uuid
                                           :wager-id   s/Uuid})

(s/defschema WagerCancelled {:event-type (s/eq :wager-cancelled)
                             :player-id  s/Uuid
                             :wager-id   s/Uuid})

(s/defschema WagerLocked {:event-type (s/eq :wager-locked)
                          :player-id  s/Uuid
                          :wager-id   s/Uuid})

(s/defschema WagerWon {:event-type (s/eq :wager-won)
                       :player-id  s/Uuid
                       :wager-id   s/Uuid})

(s/defschema WagerPushed {:event-type (s/eq :wager-pushed)
                          :player-id  s/Uuid
                          :wager-id   s/Uuid})

(s/defschema WagerLost {:event-type (s/eq :wager-lost)
                        :player-id  s/Uuid
                        :wager-id   s/Uuid})

(s/defschema WinningsEarned {:event-type (s/eq :winnings-earned)
                             :player-id  s/Uuid
                             :amount     s/Num})
