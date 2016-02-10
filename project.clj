(defproject marzoloco "0.1.0-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.0.4"]
                 [selmer "1.0.0"]
                 [markdown-clj "0.9.85"]
                 [luminus/config "0.5"]
                 [ring-middleware-format "0.7.0"]
                 [metosin/ring-http-response "0.6.5"]
                 [bouncer "1.0.0"]
                 [org.webjars/bootstrap "4.0.0-alpha.2"]
                 [org.webjars/font-awesome "4.5.0"]
                 [org.webjars.bower/tether "1.1.1"]
                 [org.webjars/jquery "2.2.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.taoensso/tower "3.0.2"]
                 [compojure "1.4.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring "1.4.0" :exclusions [ring/ring-jetty-adapter]]
                 [mount "0.1.8"]
                 [luminus-nrepl "0.1.2"]
                 [metosin/compojure-api "0.24.5"]
                 [metosin/ring-swagger-ui "2.1.4-0"]
                 [org.webjars/webjars-locator-jboss-vfs "0.1.0"]
                 [luminus-immutant "0.1.0"]
                 [luminus-log4j "0.1.2"]]

  :min-lein-version "2.0.0"
  :uberjar-name "marzoloco.jar"
  :jvm-opts ["-server"]
  :resource-paths ["resources"]

  :main marzoloco.core

  :plugins [[lein-environ "1.0.1"]]
  :profiles
  {:uberjar       {:omit-source    true
                   :env            {:production true}
                   :aot            :all
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   :project/dev   {:dependencies   [[prone "1.0.1"]
                                    [ring/ring-mock "0.3.0"]
                                    [ring/ring-devel "1.4.0"]
                                    [pjstadig/humane-test-output "0.7.1"]]


                   :source-paths   ["env/dev/clj"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options   {:init-ns user}
                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]
                   ;;when :nrepl-port is set the application starts the nREPL server on load
                   :env            {:dev        true
                                    :port       3000
                                    :nrepl-port 7000}}
   :project/test  {:env {:test       true
                         :port       3001
                         :nrepl-port 7001}}
   :profiles/dev  {}
   :profiles/test {}})
