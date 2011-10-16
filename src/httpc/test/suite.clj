(ns httpc.test.suite
  (:use [httpc.player]
	[httpc.test common simple rest stateful]
	:reload-all)
  (:gen-class))

(def *suites*)
(def *suite*)

(defn all-suites []
  (deref *suites*))

(defn all-test-in-suite []
  (:tests (deref *suite*)))

(defn switch-suite! [s]
  (let [suite (first (filter #(= (:name %) s) (all-suites)))]
    (reset! *suite* suite)))

(defn- make-suite [name tests]
  {:name name :tests tests})

(defn- random-test [p]
  (let [test-fn (rand-nth (:tests (deref *suite*)))]
   (setup-test test-fn p)))

; Rename to select tests?
(defn create-tests []
  "Create test instances for each active player. The test instance
   contains the question to send to player as well as a closure to
   validate the result."
  (doall
   (for [player (active-players)]
     (random-test player))))


(defn- init-suites []
  [(make-suite "Warmup" [test-your-name])
   (make-suite "Arithmetic" [test-two-number-arithmetic
			     test-largest-number])
   (make-suite "Basic HTTP" [test-user-agent
			     test-referer
			     test-cookies])
   (make-suite "Rest" [test-restful-resource])
   (make-suite "Sessions" [test-my-name-session
			   test-arithmetic-with-session])
   (make-suite "Dev" [test-my-name-session
		      test-arithmetic-with-session])])

(def *suites* (atom (init-suites)))
(def *suite* (atom (first @*suites*)))
