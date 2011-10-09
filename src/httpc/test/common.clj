(ns httpc.test.common
  (:use [httpc.player]
	[clojure.contrib.str-utils :only [str-join]])
  (:gen-class))

(defn- make-test [player request expectation & {:keys [save]}]
  {:player player
   :request request
   :expect expectation
   :state save})

(defn- to-question [player & {:keys [suffix params headers method]}]
  {:url (str (:url player) suffix)
   :query params
   :headers headers
   :method method})

(defn- function-name [f]
  (str (:name (meta f))))

(defn- content-equals? [content expected]
  (= (.toLowerCase (str expected)) (.. (str content) toLowerCase trim)))

(defn- is-non-positive-code? [code]
  (let [code-int (int (if code code 0))]
    (not (<= 200 code-int 299))))

(defn- if-positive-response [resp pred]
  (cond (:error resp) [:error (:error resp)]
	(is-non-positive-code? (:code (:status resp))) [:error "Server did not respond with 200"]
	:else (pred)))

(defn- assert-content [expected]
  (fn [resp]
    (let [[s msg]
	  (if-positive-response resp
				#(if (content-equals? (:content resp) expected)
				   [:ok "Correct"]
				   [:fail "Wrong answer!"]))]
      (make-log-event s msg))))

(defn- random-int [min max]
  (+ min (rand-int (- max min))))

(defn- random-ints [n min max]
  (repeatedly n #(random-int min max)))

(defn test-your-name [p & args]
  (make-test p
	     (to-question p :params {:q "What is your name?"})
	     (assert-content (:name p))))

(defn test-two-number-arithmetic [p & args]
  "Test arithmetic with two random numbers and a random operation (+, - or *)"
  (let [[a b] (random-ints 2 1 20)
	op (rand-nth [+ * -])
	op-name (function-name op)]
    (make-test p
	       (to-question p :params {:q (format "How much is %s %s %s" a op-name b)})
	       (assert-content (str (op a b))))))

(defn test-arithmetic-with-params [p & args]
  (let [[x y] (random-ints 2 1 100)
	[a b] (rand-nth [["a" "b"] ["x" "y"] ["f" "g"] ["i" "j"]])
	op (rand-nth [+ - *])
	op-name (function-name op)
	params {:q (format "How much is %s %s %s" a op-name b)
		a x
		b y}
	a x
	b y]
    (make-test p
	       (to-question p :params params)
	       (assert-content (op x y)))))

(defn test-largest-number [p & args]
  (let [ns (random-ints 5 1 1000)
	q (str "Which of the numbers is largest: " (str-join ", " ns))]
    (make-test p
	       (to-question p :params {:q q})
	       (assert-content (str (apply max ns))))))

(defn test-user-agent [p & args]
  (let [ua (rand-nth ["Mozilla/5.0 Chrome/15.0.872.0 Safari/535.2"
		      "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0"
		      "Mozilla/5.0 (X11; Linux i686; rv:6.0) Gecko/20100101 Firefox/6.0"
		      "Mozilla/5.0 (X11; en-US; rv:1.9.2.8) Gecko/20101230 Firefox/3.6.8"
		      "Googlebot/2.1 (+http://www.googlebot.com/bot.html)"])]
    (make-test p
	       (to-question p
			    :params {:q "What is my user agent"}
			    :headers {"User-Agent" ua})
	       (assert-content ua))))

(defn test-restful-resource [p & {:keys [state]}]
  (println "state before" state)
  (make-test p
	     (to-question p
			  :method :put
			  :suffix "/resource/foo")
	     (fn [& args] (make-log-event :ok "Ok"))
	     :save (assoc state :put "foo1")))

(defn setup-test [test-fn player]
  (let [test-name (function-name test-fn)
	test-state (get-in player [:test-state test-name])
	testcase (test-fn player :state test-state)]
    (assoc testcase :name test-name)))

(defn- assert-test [test resp]
  ((:expect test) resp))

(defn assert-response! [test resp]
  "Assert the received response against the expected result."
  (let [log-entry (assert-test test resp)]
    ; todo: take the state from response of assert (or both assert and test)
    (update-player-attr! (:player test) [:test-state (:name test)] (:state test))
    (record-event! (:player test) log-entry)))
