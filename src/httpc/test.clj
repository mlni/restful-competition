(ns httpc.test
  (:use [httpc.player])
  (:gen-class))

(def *suites* [])
(def *suite* (atom nil)) ; fixme: set initial value

(defn- make-test [player request expectation]
  {:player player
   :request request
   :expect expectation})

(defn- to-question [player {:keys [suffix params headers]}]
  {:url (str (:url player) suffix)
   :query params
   :headers headers})

(defn- assert-content [expected]
  (fn [resp]
    (let [[s msg]
	  (cond (:error resp) [:error (:error resp)]
		(not= 200 (:code (:status resp))) [:error "Server did not respond with 200"]
		(= expected (:content resp)) [:ok "Correct"]
		:else [:fail "Wrong answer!"])]
      (make-log-event s msg))))

(defn- random-int [min max]
  (+ min (rand-int (- max min))))

(defn test-your-name [p]
  (make-test p
	     (to-question p {:params {:q "What is your name?"}
				  :headers {:foo-bar "Bar"}})
	     (assert-content (:name p))))

(defn test-sum-numbers [p]
  (let [a (random-int 1 10)
	b (random-int 1 10)]
    (make-test p
	       (to-question p {:params {:q (str "How much is " a " + " b)}})
	       (assert-content (str (+ a b))))))

(defn test-largest-number [p]
  (let [a (random-int 1 100)
	b (random-int 1 100)
	c (random-int 1 100)]
    (make-test p
	       (to-question p {:params {:q (str "Which of the numbers is largest: "
						a ", " b ", " c)}})
	       (assert-content (str (max a b c))))))

(defn- random-test [p]
  ((rand-nth (:tests (deref *suite*))) p))

(defn- assert-test [test resp]
  ((:expect test) resp))

(defn create-tests []
  "Create test instances for each active player. The test instance
   contains the question to send to player as well as a closure to
   validate the result."
  (doall
   (for [player (active-players)]
     (random-test player))))

(defn assert-response! [test resp]
  "Assert the received response against the expected result."
  (let [log-entry (assert-test test resp)]
    (record-event! (:player test) log-entry)))

(defn switch-suite! [s]
  (let [suite (first (filter #(= (:name %) s) *suites*))]
    (reset! *suite* suite)))

(defn- make-suite [name tests]
  {:name name :tests tests})

(defn- init []
  (def *suites* [(make-suite "Trivial" [test-your-name])
		 (make-suite "Simple" [test-sum-numbers
				       test-largest-number])])
  (switch-suite! "Simple"))

(init)