(ns httpc.core
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:use [httpc player web runner]
	[ring.adapter.jetty :only [run-jetty]]
	[ring.middleware (reload :only [wrap-reload])
                         (stacktrace :only [wrap-stacktrace])
                         [file-info :only [wrap-file-info]]]
	compojure.core)
  (:gen-class))

(defroutes main-routes
  public-routes
  admin-routes
  (route/resources "/")
  (route/not-found "Page not found"))

(defn app []
  (-> #'main-routes
      handler/site))

(defn- start-webserver []
  (println "Launching jetty")
  (run-jetty (app) {:port 8080 :join? false}))

(defn- start-poller []
  (println "Launching testrunner")
  (.start (Thread. (fn [] (test-thread-main)))))

(defn -main [& args]
  (start-webserver)
  (start-poller))
