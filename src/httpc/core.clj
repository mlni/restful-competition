(ns httpc.core
  (:require [http.async.client :as c]
	    [compojure.route :as route]
            [compojure.handler :as handler])
  (:use [httpc player test web]
	[ring.adapter.jetty :only [run-jetty]]
	[ring.middleware (reload :only [wrap-reload])
                         (stacktrace :only [wrap-stacktrace])
                         [file-info :only [wrap-file-info]]]
	compojure.core)
  (:gen-class))

; testrunner

; configuration
(def *timeout* 5000)
(def *test-interval* 10000)


(defn to-response [r]
  {:headers (c/headers r)
   :content (c/string r)
   :error (c/error r)
   :status (c/status r)})

(defn start-timestamp []
  (System/currentTimeMillis))

(defn timeout? [start]
  (let [delta (- (System/currentTimeMillis) start)]
    (> delta *timeout*)))

(defn fire-request [client test]
  (let [r (:request test)]
    {:test test
     :response (c/GET client (:url r) :query (:query r) :headers (:headers r))}))

(def split-by-pred (juxt filter remove))

(defn test-loop! []
  (with-open [client (c/create-client)]
    (let [tests (create-tests)
	  responses (doall (map #(fire-request client %) tests))
	  start (start-timestamp)]
      (loop [responses responses]
	(let [[finished remaining] (split-by-pred #(c/done? (:response %)) responses)]
	  (doall remaining) ; force execution
	  (doseq [r finished]
	    (assert-response! (:test r) (to-response (:response r))))
	  (cond (empty? remaining) 'done
		(timeout? start) (record-timeout! (map :test remaining))
		:else
		(do
		  (Thread/sleep 100)
		  (recur remaining))))))))

(defn test-thread-main []
  (loop []
    (let [start (start-timestamp)]
      (println "Executing tests" (java.util.Date.))
      (test-loop!)
      (Thread/sleep (- *test-interval* (- (System/currentTimeMillis) start)))
      (recur))))

; web stuff

(defroutes main-routes
  (GET "/" [] (index-page))
  (GET "/register" [] (register-page))
  (POST "/register" {params :params} (do-register (params :name) (params :url)))
  (GET "/player/:id" {params :params} (player-page (params :id)))
  (route/resources "/")
  (route/not-found "Page not found"))

(defn app []
  (-> main-routes
      handler/site))

(defn -main [& args]
  (println "Launching jetty")
  (run-jetty (app) {:port 8080 :join? false})
  (println "Launching testrunner")
  (test-thread-main))
