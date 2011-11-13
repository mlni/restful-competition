(ns httpc.core
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
	    [swank.swank])
  (:use [httpc player web runner log fortunes]
	[ring.adapter.jetty :only [run-jetty]]
	(ring.util [response :as response])
	[ring.middleware (reload :only [wrap-reload])
                         (stacktrace :only [wrap-stacktrace])
                         [file-info :only [wrap-file-info]]]
	ring.middleware.http-basic-auth
	compojure.core
	:reload-all)
  (:gen-class))

(defroutes main-routes
  public-routes
  (route/resources "/")
  (wrap-require-auth admin-routes authenticate "Admin area" {:body "This is not part of the game"})
  (route/not-found "Page not found"))

(defn app []
  (-> #'main-routes
      wrap-authorization
      (wrap-stacktrace)
      handler/site))

(defn start-webserver []
  (println "Launching jetty")
  (run-jetty (app) {:port 8080 :join? false}))

(defn start-poller []
  (println "Launching testrunner")
  (.start (Thread. (fn [] (test-thread-main)))))

(defn start-app []
  (ensure-log-dir)
  (start-webserver)
  (start-poller))

(defn start-swank []
  (swank.swank/start-repl))

(defn -main [& args]
  (start-app)
  (try
    (start-swank)
    (catch Exception e (println "Could not start swank " e))))
