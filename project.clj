(defproject vineyard-transactions "0.1.0-SNAPSHOT"
  :description "Vineyard transactions"
  :url "http://misur.me"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.1"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [org.clojure/data.csv "1.0.0"]
                 [mysql/mysql-connector-java "5.1.39"]
                 [java-jdbc/dsl "0.1.0"]
                 [ring "1.8.0"]
                 [compojure "1.6.1"]
                 [ring/ring-mock "0.3.2"]
                 [ring/ring-core "1.8.0"]
                 [ring/ring-json "0.5.0"]
                 [ring-logger "1.0.1"]
                 [clj-time "0.15.2"]
                 [bouncer "1.0.1"]
                 [buddy "2.0.0"]
                 [buddy/buddy-core "1.6.0"]
                 [semantic-csv "0.2.1-alpha1"]]
  :main ^:skip-aot  vineyard-transactions.core
  :uberjar-name "vineyard-transactions.jar"
  :aot [vineyard-transactions.core]
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns vineyard-transactions.core})
