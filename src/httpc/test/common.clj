(ns httpc.test.common
  (:use [httpc.player])
  (:gen-class))

(defn make-test [player request expectation & {:as kv-pairs}]
  (merge {:final true}
	 (merge kv-pairs
		{:player player
		 :request request
		 :expect expectation})))

(defn to-question [& {:keys [suffix params headers method body]}]
  {:url suffix
   :query params
   :headers headers
   :method method
   :body body})

(defn function-name [f]
  (str (:name (meta f))))

(defn- content-equals? [content expected]
  (= (.toLowerCase (str expected)) (.. (str content) toLowerCase trim)))

(defn- result
  ([[s m]] (result s m))
  ([status msg]
     {:status status
      :msg msg}))

(defn- is-non-positive-code? [code]
  (let [code-int (int (if code code 0))]
    (not (<= 200 code-int 299))))

(defn respond-error
  ([] (respond-error "Server did not respond with 200"))
  ([msg] (result [:error msg])))

(defn respond-correct []
  (result [:ok "Correct"]))

(defn respond-fail
  ([] (respond-fail "Wrong answer!"))
  ([msg] (result [:fail msg])))

(defn on-success [resp pred]
  (cond (:error resp) (respond-error (:error resp))
	(is-non-positive-code? (:code (:status resp))) (respond-error)
	(map? pred) pred
	:else (pred)))

(defn assert-content [expected]
  (fn [resp & more]
    (on-success resp
		#(if (content-equals? (:content resp) expected)
		   (respond-correct)
		   (respond-fail)))))

(defn setup-test [test-fn player]
  (let [test-name (function-name test-fn)
	test-state (get-in player [:test-state test-name])
	testcase (test-fn player :state test-state)]
    (assoc testcase :name test-name)))

(defn assert-test [test resp]
  ((:expect test) resp))

(defn assert-response! [test resp]
  "Assert the received response against the expected result."
  (let [result (assert-test test resp)
	log-entry (make-log-event result)
	test-name (:name test)]
    (when (:next-state test)
      (let [ns ((:next-state test) test result resp)]
	(println "Updating state!" ns)
	(set-player-attr! (:player test) [:test-state test-name] ns)))
    (when (and (= :ok (:status result))
	       (:final test))
      (update-player-attr! (:player test) [:completed-tests] #(conj % test-name)))
    (record-event! (:player test) log-entry)))
