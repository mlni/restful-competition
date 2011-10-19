(ns httpc.test.simple
  (:use httpc.test.common
	[clojure.contrib.str-utils :only [str-join]]
	[clojure.contrib.lazy-seqs]))

(defn test-your-name [p & args]
  (make-test p
	     (to-question :params {:q "What is your name?"})
	     (assert-content (:name p))))

(defn- create-two-number-aritmetic-test [op]
  "Test arithmetic with two random numbers and a random operation (+, - or *)"
  (fn [p & args]
   (let [[a b] (random-ints 2 1 (if (< (correct-answers) 5) 20 20000))
	 op-name (function-name op)]
     (make-test p
		(to-question :params {:q (format "How much is %s %s %s" a op-name b)})
		(assert-content (str (op a b)))))))

(def test-two-numbers-sum (create-two-number-aritmetic-test +))
(def test-two-numbers-subtract (create-two-number-aritmetic-test -))
(def test-two-numbers-multiply (create-two-number-aritmetic-test *))

(defn test-two-numbers-division [p & args]
  (let [[a b] (random-ints 2 1 20)
	x (* a b)]
    (make-test p
	       (to-question :params {:q (format "How much is %s / %s" x a)})
	       (assert-content b))))

(defn test-arithmetic-with-params [p & args]
  (let [{:keys [x y a b op result]} (create-arithmetic-testcase [+ - *])
	params {:q (format "How much is %s %s %s" a op b)
		a x
		b y}]
    (make-test p
	       (to-question :params params)
	       (assert-content result)
	       :score 4)))

(defn test-largest-number [p & args]
  (let [n (if (< (correct-answers) 5) 5 10)
	ns (random-ints n 1 1000)
	q (str "Which of the numbers is largest: " (str-join ", " ns))]
    (make-test p
	       (to-question :params {:q q})
	       (assert-content (str (apply max ns)))
	       :score 2)))

(defn test-nth-fib [p & args]
  (let [n (random-int 10 (if (< (correct-answers) 5) 30 300))
	f (nth (fibs) (dec n))]
    (make-test p
	       (to-question :params {:q (format "What is the %sth number in Fibonacci sequence" n)})
	       (assert-content (str f))
	       :score 3)))

(defn test-user-agent [p & args]
  (let [ua (rand-nth ["Mozilla/5.0 Chrome/15.0.872.0 Safari/535.2"
		      "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0"
		      "Mozilla/5.0 (X11; Linux i686; rv:6.0) Gecko/20100101 Firefox/6.0"
		      "Mozilla/5.0 (X11; en-US; rv:1.9.2.8) Gecko/20101230 Firefox/3.6.8"
		      "Googlebot/2.1 (+http://www.googlebot.com/bot.html)"])]
    (make-test p
	       (to-question :params {:q "Which broswer am I using"}
			    :headers {"User-Agent" ua})
	       (assert-content ua)
	       :score 3)))

(defn test-referer [p & args]
  (let [refs ["http://webmedia.eu/company/jobs/"
	      "http://www.google.com/search?q=worst%20game%20ever"
	      "http://en.wikipedia.org/wiki/Representational_state_transfer"
	      "http://imgur.com/gallery/SkeSE"]
	rnd-ref (rand-nth refs)]
    (make-test p
	       (to-question :params {:q "Which page am I coming from"}
			    :headers {"Referer" rnd-ref})
	       (assert-content rnd-ref)
	       :score 3)))

(defn test-cookies [p & args]
  (let [{:keys [x y a b op result]} (create-arithmetic-testcase [+ - *])
	params {:q (format "How much is %s %s %s" a op b)}]
    (make-test p
	       (to-question :params params
			    :headers { "Cookie" (format "%s=%s; %s=%s" a x b y) })
	       (assert-content result)
	       :score 5)))
