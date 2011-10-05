(ns httpc.player
  (:gen-class))

(def *players* (atom {}))

(defn generate-id []
  (let [keys "0123456789abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVXYZ"]
    (apply str (map (fn [_] (rand-nth keys)) (range 16)))))

(defn make-player [name url]
  {:name name :url url :log [] :score 0 :id (generate-id)})

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
   (swap! *players* assoc (:id p) p)))

(defn score [status]
  ; status: :error :ok :fail :timeout
  (get {:ok 10
	:fail -1
	:error -10
	:timeout -10} status 0))

(defn active-players []
  (all-players))

(defn players-by-score []
  (reverse (sort-by :score (all-players))))

(defn make-log-event [status message]
  {:time (System/currentTimeMillis)
   :status status
   :message message
   :score (score status)})

(def *max-log-items* 8)

(defn record-event! [player evt]
  (println (:name player) (:status evt))
  ; todo: add truncation of log
  (dosync
   (swap! *players* update-in [(:id player) :score] + (:score evt))
   (swap! *players* update-in [(:id player) :log] conj evt)))

(defn record-timeout! [resps]
  (doseq [r resps]
    (record-event! (:player r) (make-log-event :timeout "Request timed out"))))


(defn- init []
  (reset! *players* {})
  (doseq [[name url] '[("matti" "http://localhost:4000/")
		       ("bill" "http://localhost:4001/")]]
    (add-player! name url)))

(init)
