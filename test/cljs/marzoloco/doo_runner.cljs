(ns marzoloco.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [marzoloco.core-test]))

(doo-tests 'marzoloco.core-test)

