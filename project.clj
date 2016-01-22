(defproject lunchselector "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring "1.4.0"]
                 [bidi "1.25.0"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [hiccup "1.0.5"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.postgresql/postgresql "9.4.1207.jre7"]
                 [clj-time "0.11.0"]]
  :main ^:skip-aot lunchselector.app
  :target-path "target/%s"
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler lunchselector.core/lunch-app
         :auto-reload? true
         :auto-refresh? true}
  :profiles {:uberjar {:aot :all}})
