(ns httpc.log
  (:import [java.net URLEncoder]
	   [java.io File BufferedWriter FileWriter]
	   [java.text SimpleDateFormat])
  (:use [clojure.contrib.io :only [append-writer]] ))

(defn- player-name [p]
  (URLEncoder/encode (:name p)))

(defn ensure-log-dir []
  (let [dir (File. "logs")]
   (when (not (.exists dir))
     (println "Creating log directory " (.getAbsolutePath dir))
     (.mkdir dir))))
  
(defn player-log [player fmt & args]
  (let [fname (str "logs/" (player-name player) ".log")]
   (with-open [f (BufferedWriter. (FileWriter. fname true))]
     (let [ts (.format (SimpleDateFormat. "HH:mm:ss") (java.util.Date.))]
       (.write f (str ts " "))
       (.write f (apply format fmt args))
       (.write f "\n")))))
