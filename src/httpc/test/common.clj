(ns httpc.test.common
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

(defn- content-equals? [content expected]
  (= (.toLowerCase (str expected)) (.. (str content) toLowerCase trim)))

(defn- assert-content [expected]
  (fn [resp]
    (let [[s msg]
	  (cond (:error resp) [:error (:error resp)]
		(not= 200 (:code (:status resp))) [:error "Server did not respond with 200"]
		(content-equals? (:content resp) expected) [:ok "Correct"]
		:else [:fail "Wrong answer!"])]
      (make-log-event s msg))))

(defn- random-int [min max]
  (+ min (rand-int (- max min))))

(defn- random-ints [n min max]
  (repeatedly n #(random-int min max)))

(defn test-your-name [p]
  (make-test p
	     (to-question p {:params {:q "What is your name?"}
				  :headers {:foo-bar "Bar"}})
	     (assert-content (:name p))))

(defn- two-number-arithmetic [p msg op]
  (let [[a b] (random-ints 2 1 20)]
    (make-test p
	       (to-question p {:params {:q (format msg  a b)}})
	       (assert-content (str (op a b))))))

(defn test-sum-numbers [p]
  (two-number-arithmetic p "How much is %s + %s" +))

(defn test-mul-numbers [p]
  (two-number-arithmetic p "How much is %s * %s" *))

(defn test-subtract-numbers [p]
  (two-number-arithmetic p "How much is %s - %s" -))

(defn test-largest-number [p]
  (let [ns (random-ints 5 1 1000)]
    (make-test p
	       (to-question p {:params {:q (str "Which of the numbers is largest: "
						(apply str (interpose ", " ns)))}})
	       (assert-content (str (apply max ns))))))

(defn- assert-test [test resp]
  ((:expect test) resp))

(defn assert-response! [test resp]
  "Assert the received response against the expected result."
  (let [log-entry (assert-test test resp)]
    (record-event! (:player test) log-entry)))
