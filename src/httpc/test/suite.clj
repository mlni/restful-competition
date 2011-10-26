(ns httpc.test.suite
  (:use [httpc.player]
	[httpc.test common simple rest stateful]
	:reload-all)
  (:gen-class))

(def *suites*)
(def *suite*)
(def *all-tests*)

(defn all-suites []
  (deref *suites*))

(defn all-tests-in-suite []
  (:tests (deref *suite*)))

(defn- all-tests []
  *all-tests*)

(defn suite-test-names []
  (map function-name (all-tests-in-suite)))

(defn switch-suite! [s]
  (let [suite (first (filter #(= (:name %) s) (all-suites)))]
    (reset! *suite* suite)))

(defn- make-suite [name tests]
  {:name name :tests tests})

(defn- make-suites [& suites]
  (let [all-tests (set (mapcat :tests suites))]
    (concat suites
	  [(make-suite "Everything at once" (vec all-tests))])))

(defn- random-test [p]
  "Pick a random test and set it up for sending to a player.
   Sends an already completed test in 15% of cases, just to keep the spritis up."
  (let [all-completed-tests (completed-test-names p)
	suite (deref *suite*)
	tests (if (and (seq all-completed-tests)
		       (< (rand) 0.15))
		(vals (select-keys (all-tests) all-completed-tests))
		(:tests suite))
	test-fn (rand-nth tests)]
   (setup-test test-fn p)))

(defn- init-all-tests [suites]
  (reduce (fn [r suite]
	    (reduce (fn [r test]
		      (assoc r (function-name test) test))
		    r
		    (:tests suite)))
	  {}
	  suites))

; Rename to select tests?
(defn create-tests []
  "Create test instances for each active player. The test instance
   contains the question to send to player as well as a closure to
   validate the result."
  (doall
   (for [player (active-players)]
     (random-test player))))

(defn- init-suites []
  (make-suites
   (make-suite "Warmup" [test-your-name])
   (make-suite "Arithmetic 1" [test-largest-number
			       test-two-numbers-sum])
   (make-suite "Arithmetic 2" [test-two-numbers-multiply
			       test-two-numbers-subtract
			       test-two-numbers-division
			       test-nth-fib
			       test-arithmetic-with-params])
   (make-suite "Basic HTTP" [test-user-agent
			     test-referer
			     test-cookies])
   (make-suite "Sessions" [test-my-name-session
			   test-arithmetic-with-session])
   (make-suite "Rest" [test-restful-resource
		       test-content-types])))

(defonce *suites* (atom (init-suites)))
(defonce *suite* (atom (first @*suites*)))
(defonce *all-tests* (init-all-tests @*suites*))