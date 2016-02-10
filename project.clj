(defproject lunchselector "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [http-kit "2.1.18"]
                 [stylefruits/gniazdo "0.4.1"]
                 [bidi "1.25.0"]
                 [cheshire "5.5.0"]
                 [hiccup "1.0.5"]
                 [clj-time "0.11.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.postgresql/postgresql "9.4.1207.jre7"]
                 [commons-validator/commons-validator "1.4.0"]]
  :main ^:skip-aot lunchselector.app
  :target-path "target/%s"
  :plugins [[lein-ring "0.9.7"]]
  :profiles {:uberjar {:aot :all}})
