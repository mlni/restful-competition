(defproject httpc "1.0.0-SNAPSHOT"
  :description "Your project description"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [http.async.client "0.3.1"]
                 [compojure "0.6.4"]
		 [ring "0.3.10"]
		 [hiccup "0.3.6"]]
  :dev-dependencies [[lein-clojars "0.5.0-SNAPSHOT"]
                     [swank-clojure "1.2.1"]]  
  :namespace [httpc]
  :main httpc.core)
