(ns httpc.runner
  (:import [java.text SimpleDateFormat])
  (:require [http.async.client :as c])
  (:use [httpc player log fortunes]
	[httpc.test common suite]
	:reload-all)
  (:gen-class))

(def *timeout* 5000)
(def *test-interval* 10000)
(def *terminate* (atom false))

(defn to-response [r]
  {:headers (c/headers r)
   :content (c/string r)
   :error (c/error r)
   :status (c/status r)})

(defn- log [& msg]
  (let [ts (.format (SimpleDateFormat. "HH:mm:ss") (java.util.Date.))]
    (apply println (concat [ts] msg))))

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
	headers (merge {"User-Agent" "RESTful Competition/1.0"
			"X-Bender" (next-bender-fortune)}
		       (:headers r))]
    (try
      (player-log (:player test) "<- %s %s %s %s" method-name url (:query r) headers)
      {:test test
       :response (method client url
			 :query (:query r) :headers headers :body (:body r)
			 :timeout (+ 1000 *timeout*))}
      (catch Exception e
	(do
	  (player-log (:player test) "request failed %s" e)
	  (log "Exception while firing a request " e)
	  (.printStackTrace e)
	  nil)))))


(defn- split-done [rs]
  "Split responses by done? predicate into finished and in-progress requests"
  (loop [finished [] remaining [] rs rs]
    (if (empty? rs)
      [finished remaining]
      (if (c/done? (:response (first rs)))
	(recur (conj finished (first rs)) remaining (rest rs))
	(recur finished (conj remaining (first rs)) (rest rs))))))

(defn- test-loop! []
  (with-open [client (c/create-client)]
    (let [tests (create-tests)
	  responses (doall (filter identity
				   (map #(fire-request client %) tests)))
	  start (start-timestamp)]
      (log "Testing" (count tests) (sort (map #(get-in % [:player :name]) tests)))
      (loop [responses responses responded #{}]
	(let [[finished remaining] (split-done responses)
	      handled (doall
		       (for [r finished
			     :when (not (contains? responded r))]
			 (let [resp (to-response (:response r))]
			   (player-log (get-in r [:test :player]) "-> %s" resp)
			   (assert-response! (:test r) resp)
			   r)))
	      responded (reduce conj responded handled)]
	  (doall remaining)		; force execution
	  (cond (empty? remaining) (do
				     (log "... finished")
				     'done)
		(timeout? start) (do
				   (record-timeout! (map :test remaining))
				   (recur nil responded))
		:else
		(do
		  (Thread/sleep 100)
		  (recur remaining responded))))))))

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
	  (Thread/sleep delay))
	(when (not (terminate?))
	  (recur))))))
