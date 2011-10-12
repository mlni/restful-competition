(ns httpc.player
  (:gen-class))

(def *max-log-items* 20)

(def *players* (ref {}))

(defn generate-id []
  (let [keys "0123456789abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVXYZ"]
    (apply str (map (fn [_] (rand-nth keys)) (range 16)))))

(defn make-player [name url]
  {:name name :url url :log '() :score 0 :id (generate-id)})

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

(defn make-log-event [result]
  (let [{status :status msg :msg} result]
    {:time (System/currentTimeMillis)
     :status status
     :message (str msg)
     :score (score status)}))

(defn record-event! [player evt]
  (when (@*players* (:id player))
   (dosync
    (alter *players* update-in [(:id player) :score] + (:score evt))
    (alter *players* update-in [(:id player) :log] #(take *max-log-items*
							  (conj % evt))))))

(defn record-timeout! [resps]
  (doseq [r resps]
    (record-event! (:player r) (make-log-event {:status :timeout :msg "Request timed out"}))))

(defn reset-scores! []
  (dosync
   (let [ps (deref *players*)]
     (alter *players*
	    (reduce (fn [r k] (assoc-in r [k :score] 0)) ps (keys ps))))))

(defn save-data! []
  (let [data (binding [*print-dup* true]
	       (print-str @*players*))]
   (spit "data/players.txt" data)))

(defn load-data! []
  (let [data (read-string (slurp "data/players.txt"))]
    (dosync
     (alter *players* data))))

(defn update-player-attr! [p path val]
  (let [key (concat [(:id p)] path)]
    (dosync
     (alter *players* assoc-in key val))))

(defn- init []
  (dosync
   (alter *players* {})
   (doseq [[name url] '[("matti" "http://localhost:4000/")]]
     (add-player! name url))))

(init)
