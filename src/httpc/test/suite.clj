(ns httpc.test.suite
  (:use [httpc.player]
	[httpc.test common simple dates rest stateful]
	clojure.set
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

(defn- completed-tests-in-suite [p]
  (count (intersection (set (completed-test-names p))
		       (set (suite-test-names)))))

(defn- pick-tests-with-treshold [tests num-completed]
  "Pick tests to use in the round based on the number of tests already completed
   in current suite."
  (let [num-tests-to-use (+ 2 (int (* 1.5 num-completed)))]
    (take num-tests-to-use tests)))

(defn- random-test [p]
  "Pick a random test and set it up for sending to a player.
   Takes into account the number of tests in the suite the user has alread completed and
   limits the number of unsolved tests in the air at once.
   Sends an already completed test in 15% of cases, just to keep the spirits up."
  (let [all-completed-tests (completed-test-names p)
	has-completed-any (seq all-completed-tests)
	suite (deref *suite*)
	completed-in-suite (completed-tests-in-suite p)
	tests (if (and has-completed-any
		       (< (rand) 0.15))
		(vals (select-keys (all-tests) all-completed-tests))
		(pick-tests-with-treshold (:tests suite) completed-in-suite))
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
   (make-suite "New tests" [; test-two-number-sum-hex
			    ; test-two-number-mul-hex
			    ; test-nth-factorial
			    ; test-greatest-common-divisors
			    ; test-days-between
			    ; test-weekday-of-a-date
			    test-earliest-date])
   (make-suite "Rest" [test-restful-resource
		       test-content-types])))

(defonce *suites* (atom (init-suites)))
(defonce *suite* (atom (first @*suites*)))
(defonce *all-tests* (init-all-tests @*suites*))