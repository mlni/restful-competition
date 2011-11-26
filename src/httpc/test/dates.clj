(ns httpc.test.dates
  (:use httpc.player
	[httpc.test common]
	:reload-all)
  (:import [java.util Date Calendar]
	   [java.text SimpleDateFormat])
  (:gen-class))

(defn- days-ago [n]
  (let [cal (doto (Calendar/getInstance)
	      (.set Calendar/HOUR 0)
	      (.set Calendar/MINUTE 0)
	      (.set Calendar/SECOND 0)
	      (.set Calendar/MILLISECOND 0)
	      (.add Calendar/DAY_OF_YEAR (- n)))
	d (.getTime cal)]
    d))

(defn- abs [n]
  (if (< n 0) (* -1 n) n))

(defn- format-ee [d]
  (.format (SimpleDateFormat. "dd.MM.yyyy") d))

(defn- format-us [d]
  (.format (SimpleDateFormat. "dd.MM.yyyy") d))

(defn test-days-between [& args]
  (let [[n1 n2] (random-ints 2 1 5000)
	d1 (days-ago n1)
	d2 (days-ago n2)
	[d1 d2] (complicate (sort [d1 d2]) [d1 d2])
	fmt (complicate format-ee (rand-nth [format-ee format-us]))
	r (abs (int (/ (- (.getTime d1) (.getTime d2)) (* 24 60 60 1000))))]
    (println "days between r " n1 n2 r)
    (make-test (to-question :params {:q (format "How many days are between %s and %s"
						(fmt d1)
						(fmt d2))})
	       (assert-content r)
	       :score 2)))