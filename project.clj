(defproject clojo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [clj-http "2.0.0"]
                 [org.clojure/data.json "0.2.6"]
                 [cheshire "5.5.0"]
                 [clj-time "0.11.0"]
                 ]
  :main ^:skip-aot clojo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
