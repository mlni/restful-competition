(ns httpc.test.suite
  (:use [httpc.player]
	[httpc.test common])
  (:gen-class))

(def *suites* (atom []))
(def *suite* (atom nil)) ; fixme: set initial value

(defn all-suites []
  (deref *suites*))

(defn switch-suite! [s]
  (let [suite (first (filter #(= (:name %) s) (all-suites)))]
    (reset! *suite* suite)))

(defn- make-suite [name tests]
  {:name name :tests tests})

(defn- random-test [p]
  (let [test-fn (rand-nth (:tests (deref *suite*)))]
   (setup-test test-fn p)))

; rename to select tests?
(defn create-tests []
  "Create test instances for each active player. The test instance
   contains the question to send to player as well as a closure to
   validate the result."
  (doall
   (for [player (active-players)]
     (random-test player))))


(defn- init []
  (reset! *suites* [(make-suite "Trivial" [test-your-name])
		    (make-suite "Simple" [test-two-number-arithmetic
					  test-arithmetic-with-params
					  test-largest-number
					  test-user-agent
					  ])
		    (make-suite "Dev" [test-restful-resource])])
  (switch-suite! "Dev"))

(init)
