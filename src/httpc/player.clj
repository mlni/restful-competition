(ns httpc.player
  (:import java.io.File)
  (:use httpc.log)
  (:gen-class))

(def *max-log-items* 20)
(def *error-delay* (* 20 1000))
(def *players* (ref {}))


(defn generate-random-str [len]
  (let [keys "0123456789abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVXYZ"]
    (apply str (map (fn [_] (rand-nth keys)) (range len)))))

(defn generate-id []
  (generate-random-str 16))

(defn make-player [name url]
  {:name name :url url :log '() :score 0 :id (generate-id)
   :completed-tests {}})

(defn- all-players []
  (-> *players*
      deref
      vals))

(defn player-exists? [name]
  (not (empty?
	(filter #(= name (:name %1)) (all-players)))))

(defn player-by-id [id]
  (get (deref *players*) id))

(defn add-player! [name url]
  (let [p (make-player name url)]
    (dosync
     (alter *players* assoc (:id p) p))))

(defn remove-player! [id]
  (dosync
   (alter *players* dissoc id)))

(defn score [result]
  ; status: :error :ok :fail :timeout
  (case (:status result)
	:ok (get result :test-score 1)
	:fail (get result :test-penalty -1)
	:error -10
	:timeout -10
	0))

(defn active-players []
  (let [now (System/currentTimeMillis)]
    (filter (fn [player]
	      (let [next (:next-request player)]
		(or (nil? next)
		    (> now next))))
	    (all-players))))

(defn players-by-score []
  (reverse (sort-by :score (all-players))))

(defn make-log-event [result]
  (let [{status :status msg :msg} result]
    (when (not (and msg status))
      (throw IllegalArgumentException (str "Cannot log event, status/message missing: " msg status)))
    {:time (System/currentTimeMillis)
     :status status
     :message (str msg)
     :score (score result)}))

(defn- error-timeout []
  (+ (System/currentTimeMillis) *error-delay*))

(defn record-event! [player evt]
  (when (@*players* (:id player))
   (dosync
    (alter *players* update-in [(:id player) :score] + (:score evt))
    (alter *players* update-in [(:id player) :log] #(take *max-log-items*
							  (conj % evt)))
    (when (#{:timeout :error} (:status evt))
      (alter *players* assoc-in [(:id player) :next-request] (error-timeout))))))

(defn record-timeout! [resps]
  (doseq [r resps]
    (record-event! (:player r) (make-log-event {:status :timeout :msg "Request timed out"}))
    (player-log (:player r) "Timed out")))

(defn- set-all-attrs [attr val]
  (fn [ps]
    (reduce (fn [r k] (assoc-in r [k attr] val)) ps (keys ps))))

(defn reset-scores! []
  (dosync
   (alter *players* (set-all-attrs :score 0))))

(defn reset-progress! []
  (dosync
   (alter *players* (set-all-attrs :completed-tests {}))))

(defn save-data! []
  (let [dir (File. "data")
	data (binding [*print-dup* true]
	       (print-str @*players*))]
    (when (not (.exists dir))
      (.mkdir dir))
    (spit "data/players.txt" data)))

(defn load-data! []
  (let [data (read-string (slurp "data/players.txt"))]
    (dosync
     (ref-set *players* data))))

(defn set-player-attr! [p path val]
  (let [key (concat [(:id p)] path)]
    (dosync
     (when (contains? @*players* (:id p))
      (alter *players* assoc-in key val)))))

(defn update-player-attr! [p path f]
  (let [key (concat [(:id p)] path)]
    (dosync
     (when (contains? @*players* (:id p))
      (alter *players* update-in key f)))))

(defn- init []
  (dosync
   (alter *players* {})
   (doseq [[name url] '[("matti" "http://localhost:4000/")]]
     (add-player! name url))))

(init)
