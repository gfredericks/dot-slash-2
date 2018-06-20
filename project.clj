(defproject com.gfredericks/dot-slash-2 "0.1.3"
  :description "Clojure library for easily creating proxy namespaces"
  :url "https://github.com/gfredericks/dot-slash-2"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :deploy-repositories [["releases" :clojars]]
  :profiles {:clojure-18
             {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :clojure-17
             {:dependencies [[org.clojure/clojure "1.7.0"]]}}
  :aliases {"test-all"
            ["do"
             ["clean"]
             ["test"]
             ["with-profile" "+clojure-18" "test"]
             ["with-profile" "+clojure-17" "test"]]})
