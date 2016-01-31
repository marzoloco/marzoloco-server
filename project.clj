(defproject marzoloco "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [prismatic/schema "1.0.4"]]
  :main ^:skip-aot marzoloco.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
