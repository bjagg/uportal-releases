(defproject uportal-releases "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.10.0"]
                 [cheshire "5.8.1"]
                 [environ "1.1.0"] ]
  :plugins [[lein-environ "1.1.0"]]
  :main ^:skip-aot uportal-releases.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
