(ns httpc.test.dates
  (:use httpc.player
	[httpc.test common]
	[clojure.contrib.str-utils :only [str-join]]
	:reload-all)
  (:import [java.util Date Calendar Locale]
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
  (.format (SimpleDateFormat. "yyyy-MM-dd") d))

(defn test-days-between [& args]
  (let [[n1 n2] (random-ints 2 1 5000)
	d1 (days-ago n1)
	d2 (days-ago n2)
	[d1 d2] (complicate (sort [d1 d2]) [d1 d2])
	fmt (complicate format-ee (rand-nth [format-ee format-us]))
	r (abs (int (/ (- (.getTime d1) (.getTime d2)) (* 24 60 60 1000))))]
    (simple-question (format "How many days are between %s and %s"
			     (fmt d1)
			     (fmt d2))
		     r
		     :score 2)))

(defn test-weekday-of-a-date [& args]
  (let [d (days-ago (random-int 100 5000))
	weekday (.format (SimpleDateFormat. "EEEE" Locale/US) d)
	fmt (complicate format-ee (rand-nth [format-ee format-us]))]
    (simple-question (format "What was the weekday of %s"
			     (fmt d))
		     weekday)))

(defn test-earliest-date [& args]
  (let [ns (random-ints 5, 100 5000)
	days (map days-ago ns)
	fmt (complicate format-ee (rand-nth [format-ee format-us]))]
    (simple-question (format "Which of the following days is the earliest: %s"
			     (str-join ", " (map fmt days)))
		     (fmt (first (sort days))))))
