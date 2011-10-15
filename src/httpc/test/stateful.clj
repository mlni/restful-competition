(ns httpc.test.stateful
  (:use httpc.test.common
	httpc.test.session))


; test remembering my name

(defn test-my-name-session [p & {sessions :state testname :test-name}]
  (letfn [(init [s]
		(let [name (rand-nth ["Matti" "Bill" "Bob" "John" "Steve"
				      "Donald" "Dennis" "Rob" "Edsger" "Martin"])]
		  (-> s
		      (assoc :expected name)
		      (assoc :question (format "My name is %s. What is my name" name)))))
	  (resend [s]
		  (assoc s :question "What is my name"))]
   (let [correct-answers (get-in p [:completed-tests testname] 0)
	 parallel-sessions (if (> correct-answers 5) 3 1)
	 workflow [init
		   resend
		   resend]]
     (session-testcase p sessions workflow parallel-sessions))))


; test arithmetics with sessions

(defn test-arithmetic-with-session [p & {sessions :state testname :test-name}]
  (letfn [(init [s]
		(let [{:keys [x y a b op result]} (create-arithmetic-testcase)]
		  (merge s {:param1 a :param2 b :val1 x :val2 y :op-name op :result result
			    :expected x :question (format "Let %s be %s. What is %s" a x a)})))
	  (arg2 [s]
		(let [{:keys [param2 val2]} s]
		  (-> s
		      (assoc :question (format "Let %s be %s. What is %s" param2 val2 param2))
		      (assoc :expected val2))))
	  (result [s]
		  (let [{:keys [param1 param2 op-name result]} s]
		    (-> s
			(assoc :question
			  (format "Remember how much is %s %s %s" param1 op-name param2))
			(assoc :expected result))))]
    
   (let [corrects (get-in p [:completed-tests testname] 0)
	 num-sessions (if (>= corrects 5) 3 1)
	 workflow [init
		   arg2
		   result]]
     (session-testcase p sessions workflow num-sessions))))