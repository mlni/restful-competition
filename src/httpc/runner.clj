(ns httpc.runner
  (:require [http.async.client :as c])
  (:use [httpc player test])
  (:gen-class))

(def *timeout* 5000)
(def *test-interval* 5000)

(defn to-response [r]
  {:headers (c/headers r)
   :content (c/string r)
   :error (c/error r)
   :status (c/status r)})

(defn- start-timestamp []
  (System/currentTimeMillis))

(defn- timeout? [start]
  (let [delta (- (System/currentTimeMillis) start)]
    (> delta *timeout*)))

(defn- fire-request [client test]
  (let [r (:request test)]
    {:test test
     :response (c/GET client (:url r) :query (:query r) :headers (:headers r))}))

(def split-by-pred (juxt filter remove))

(defn- test-loop! []
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
      (test-loop!)
      (let [delay (- *test-interval* (- (System/currentTimeMillis) start))]
	(when (pos? delay)
	 (Thread/sleep delay)))
      (recur))))
