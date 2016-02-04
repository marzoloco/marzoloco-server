(ns marzoloco.wagering.commands
  (:require [schema.core :as s]))

;; should there be a Wager value "object" here that is used by most commands?
;; I don't think so, because only the PlaceWager command needs the verbose Wager description

(s/defrecord PlaceWager
  [player-id :- s/Uuid
   wager-id :- s/Uuid
   game-id :- s/Uuid
   ; may not need game-id since it's implied by the bet-id
   bet-id :- s/Uuid
   side :- (s/enum :a :b)
   ; looking for better language to generically describe the sides of a bet
   amount :- BigDecimal])

;; It's obvious that bet-id and side have to be in this command, but they're not currently
;; part of the player aggregate. Do they belong in that aggregate, or is there a different
;; aggregate that turns results into win/loss for a given wager?
