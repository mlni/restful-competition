(ns httpc.runner
  (:require [http.async.client :as c])
  (:use [httpc player log]
	[httpc.test common suite]
	:reload-all)
  (:gen-class))

(def *timeout* 5000)
(def *test-interval* 5000)
(def *terminate* (atom false))

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

(defn terminate-poller! []
  (reset! *terminate* true))

(defn- terminate? []
  @*terminate*)

(defn- concatenate-url [prefix suffix]
  (if (and suffix
	   (.endsWith prefix "/")
	   (.startsWith suffix "/"))
    (str prefix (subs suffix 1))
    (str prefix suffix)))

(defn- fire-request [client test]
  (let [r (:request test)
	url (concatenate-url (get-in test [:player :url]) (:url (:request test)))
	method-name (or (:method r) :get)
	method (get {:get c/GET :put c/PUT
		     :post c/POST :delete c/DELETE
		     :head c/HEAD} method-name)
	headers (merge {"User-Agent" "RESTful Competition/1.0"} (:headers r))]
    (player-log (:player test) "<- %s %s %s %s" method-name url (:query r) headers)
    {:test test
     :response (method client url
		       :query (:query r) :headers headers :body (:body r)
		       :timeout (+ 1000 *timeout*))}))

(def split-by-pred (juxt filter remove))

(defn- test-loop! []
  (with-open [client (c/create-client)]
    (let [tests (create-tests)
	  responses (doall (map #(fire-request client %) tests))
	  start (start-timestamp)]
      (loop [responses responses]
	(let [[finished remaining] (split-by-pred #(c/done? (:response %)) responses)]
	  (doall remaining)		; force execution
	  (doseq [r finished]
	    (let [resp (to-response (:response r))]
	      (player-log (get-in r [:test :player]) "-> %s" resp)
	      (assert-response! (:test r) resp)))
	  (cond (empty? remaining) 'done
		(timeout? start) (record-timeout! (map :test remaining))
		:else
		(do
		  (Thread/sleep 100)
		  (recur remaining))))))))

(defn test-thread-main []
  (reset! *terminate* false)
  (loop []
    (let [start (start-timestamp)]
      (try
	(test-loop!)
	(catch Exception e
	  (.printStackTrace e)))
      (let [delay (- *test-interval* (- (System/currentTimeMillis) start))]
	(when (pos? delay)
	  (Thread/sleep delay)))
      (when (not (terminate?))
	(recur)))))
