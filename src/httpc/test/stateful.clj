(ns httpc.test.stateful
  (:use httpc.test.common
	httpc.test.session))

(defn- question [fmt & args]
  (apply format (concat [fmt] args)))

(defn test-my-name-session [p & {sessions :state}]
  "Remember my name"
  (letfn [(init [s]
		(let [name (rand-nth ["Matti" "Bill" "Bob" "John" "Steve"
				      "Donald" "Dennis" "Rob" "Edsger" "Martin"])]
		  (-> s
		      (assoc :expected name)
		      (assoc :question (question "My name is %s. What is my name" name)))))
	  (resend [s]
		  (-> s
		      (assoc :question (question "What is my name"))
		      (assoc :score 4)))]
   (let [parallel-sessions (if (> (correct-answers) 5) 3 1)
	 wf (workflow [init
		       resend
		       resend])]
     (session-testcase p sessions wf parallel-sessions))))


(defn test-arithmetic-with-session [p & {sessions :state}]
  "Remember values of parameters and calculate the value an arithmetic expression"
  (letfn [(init [s]
		(let [{:keys [x y a b op result]} (create-arithmetic-testcase [* - +])]
		  (merge s {:param1 a :param2 b :val1 x :val2 y :op-name op :result result
			    :expected x :question (question "Let %s be %s. What is %s" a x a)})))
	  (arg2 [s]
		(let [{:keys [param2 val2]} s]
		  (-> s
		      (assoc :question (question "Let %s be %s. What is %s" param2 val2 param2))
		      (assoc :expected val2))))
	  (result [s]
		  (let [{:keys [param1 param2 op-name result]} s]
		    (-> s
			(assoc :question
			  (question "Remember how much is %s %s %s" param1 op-name param2))
			(assoc :expected result)
			(assoc :score 5))))]
    
   (let [corrects (correct-answers)
	 num-sessions (if (>= corrects 5) 3 1)
	 wf (workflow [init
		       arg2
		       result])]
     (session-testcase p sessions wf num-sessions))))
