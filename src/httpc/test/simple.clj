(ns httpc.test.simple
  (:require [clojure.contrib.base64 :as b64])
  (:use httpc.test.common
	[clojure.contrib.str-utils :only [str-join]]
	[clojure.contrib.lazy-seqs]
	[clojure.contrib.math :only [gcd]]))

(defn test-your-name [& args]
  (make-test (to-question :params {:q "What is your name?"})
	     (assert-content (:name (current-player)))))

(defn- create-two-number-aritmetic-test [op & {convert :convert score :score}]
  "Test arithmetic with two random numbers and a random operation (+, - or *)"
  (fn [& args]
    (let [[a b] (random-ints 2 1 (if (< (correct-answers) 5) 20 20000))
	  converter (or convert identity)
	  op-name (function-name op)]
      (simple-question (format "How much is %s %s %s" (converter a)
			       op-name (converter b))
		       (converter (op a b))
		       :penalty -1
		       :score (or score 1)))))

(defn test-two-numbers-sum [& args]
  ((create-two-number-aritmetic-test +)))
(defn test-two-numbers-subtract [& args]
  ((create-two-number-aritmetic-test -)))
(defn test-two-numbers-multiply [& args]
  ((create-two-number-aritmetic-test *)))

(defn- to-hex [i]
  (str "0x" (Integer/toHexString i)))

(defn test-two-number-sum-hex [& args]
  ((create-two-number-aritmetic-test + :convert to-hex :score 2)))
(defn test-two-number-mul-hex [& args]
  ((create-two-number-aritmetic-test * :convert to-hex :score 2)))
(defn test-two-number-subtract-hex [& args]
  ((create-two-number-aritmetic-test - :convert to-hex :score 2)))


(defn test-two-numbers-division [& args]
  (let [[a b] (random-ints 2 1 20)
	x (* a b)]
    (simple-question (format "How much is %s / %s" x a)
		     b)))

(defn test-arithmetic-with-params [& args]
  (let [{:keys [x y a b op result]} (create-arithmetic-testcase [+ - *])
	params {:q (format "How much is %s %s %s" a op b)
		a x
		b y}]
    (make-test (to-question :params params)
	       (assert-content result)
	       :score 4
	       :penalty -1)))

(defn test-largest-number [& args]
  (let [n (complicate 5 10)
	ns (random-ints n 1 1000)
	q (str "Which of the numbers is largest: " (str-join ", " ns))]
    (simple-question q
		     (apply max ns)
		     :score 2
		     :penalty -1)))

(defn test-second-largest-number [& args]
  (let [ns (random-ints 5 1 1000)
	ans (second (sort ns))
	fmt (complicate identity (rand-nth [identity to-hex]))]
    (simple-question (str "Which of the numbers is second largest: " (str-join ", " (map fmt ns)))
		     (fmt ans)
		     :score 2)))

(defn test-nth-fib [& args]
  (let [n (random-int 10 (complicate 30 300))
	f (nth (fibs) n)]
    (simple-question (format "What is the %sth number in Fibonacci sequence"
			     n)
		     (str f)
		     :score 3
		     :penalty -2)))

(defn test-nth-factorial [& args]
  (let [n (random-int 3 (complicate 11 30))
	r (apply * (range 1 (inc n)))]
    (simple-question (format "What is the factorial of %s" n)
		     r
		     :score 3
		     :penalty -2)))

(defn test-greatest-common-divisors [& args]
  (let [[x y] (random-ints 2 3 (complicate 30 1000000))
	r (gcd x y)]
    (simple-question (format "What is the greatest common divisor of %s and %s"
			     x y)
		     r
		     :score 3)))

(defn test-user-agent [& args]
  (let [uas ["Mozilla/5.0 Chrome/15.0.872.0 Safari/535.2"
	     "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0"
	     "Mozilla/5.0 (X11; Linux i686; rv:6.0) Gecko/20100101 Firefox/6.0"
	     "Mozilla/5.0 (X11; en-US; rv:1.9.2.8) Gecko/20101230 Firefox/3.6.8"
	     "Googlebot/2.1 (+http://www.googlebot.com/bot.html)"]
	ua (complicate (first uas) (rand-nth uas))]
    (make-test (to-question :params {:q "Which browser am I using"}
			    :headers {"User-Agent" ua})
	       (assert-content ua)
	       :score 3
	       :penalty -2)))

(defn test-referer [& args]
  (let [refs ["http://webmedia.eu/company/jobs/"
	      "http://www.google.com/search?q=worst%20game%20ever"
	      "http://en.wikipedia.org/wiki/Representational_state_transfer"
	      "http://imgur.com/gallery/SkeSE"]
	rnd-ref (complicate (first refs) (rand-nth refs))]
    (make-test (to-question :params {:q "Which page am I coming from"}
			    :headers {"Referer" rnd-ref})
	       (assert-content rnd-ref)
	       :score 3
	       :penalty -2)))

(defn test-cookies [& args]
  (let [{:keys [x y a b op result]} (create-arithmetic-testcase [+ - *])
	params {:q (format "How much is %s %s %s" a op b)}]
    (make-test (to-question :params params
			    :headers { "Cookie" (format "%s=%s; %s=%s" a x b y) })
	       (assert-content result)
	       :score 5
	       :penalty -1)))

(defn test-http-authorization [& args]
  (let [usernames ["sexykitty13" "billg" "mattij" "test" "johndoe2"]
	passwords ["kalamaja" "kamajahu" "secret" "password" "qwerty"]
	pick (complicate first rand-nth)
	[user pass] [(pick usernames) (pick passwords)]
	user-pass (b64/encode-str (str user ":" pass))]
    (make-test (to-question :params {:q "What is my username"}
			    :headers {"Authorization" (str "Basic " user-pass)})
	       (assert-content user)
	       :score 5)))
