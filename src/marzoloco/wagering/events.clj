(ns marzoloco.wagering.events
  (:require [schema.core :as s]))

(s/defrecord PointsDeposited [player-id :- s/Uuid
                              amount :- BigDecimal])

(s/defrecord WagerPlaced [player-id :- s/Uuid
                          wager-id :- s/Uuid
                          amount :- BigDecimal
                          odds :- BigDecimal])

(s/defrecord OverdrawAttempted [player-id :- s/Uuid
                                wager-id :- s/Uuid])

(s/defrecord WagerWithdrawn [player-id :- s/Uuid
                             wager-id :- s/Uuid])

(s/defrecord LockedWagerWithdrawAttempted [player-id :- s/Uuid
                                           wager-id :- s/Uuid])

(s/defrecord WagerCancelled [player-id :- s/Uuid
                             wager-id :- s/Uuid])

(s/defrecord WagerLocked [player-id :- s/Uuid
                          wager-id :- s/Uuid])

(s/defrecord WagerWon [player-id :- s/Uuid
                       wager-id :- s/Uuid])

(s/defrecord WagerPushed [player-id :- s/Uuid
                          wager-id :- s/Uuid])

(s/defrecord WagerLost [player-id :- s/Uuid
                        wager-id :- s/Uuid])

(s/defrecord WinningsEarned [player-id :- s/Uuid
                             amount :- BigDecimal])
