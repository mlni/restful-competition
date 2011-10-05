(ns httpc.test
  (:use [httpc.player])
  (:gen-class))


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
    (cond (not= 200 (:code (:status resp))) (make-log-event :error "Server did not respond with 200 OK")
	  (= expected (:content resp)) (make-log-event :ok "Test passed")
	  :else (make-log-event :fail "Test failed"))))

(defn test-your-name [p]
  (make-test p
	     (to-question p {:params {:q "What is your name?"}
				  :headers {:foo-bar "Bar"}})
	     (assert-content (:name p))))

(defn test-sum-numbers [p]
  (let [a (inc (rand-int 10))
	b (inc (rand-int 10))]
    (make-test p
	       (to-question p {:params {:q (str "How much is " a " + " b)}})
	       (assert-content (str (+ a b))))))

(defn random-test [p]
  ((rand-nth [test-your-name test-sum-numbers]) p))

(defn create-tests []
  (doall
   (for [player (active-players)]
     (random-test player))))

(defn assert-test [test resp]
  ((:expect test) resp))

(defn assert-response! [test resp]
  (let [log-entry (assert-test test resp)]
    (record-event! (:player test) log-entry)))
