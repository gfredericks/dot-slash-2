(ns com.gfredericks.dot-slash-2-test
  (:require [com.gfredericks.dot-slash-2 :as sut]
            [clojure.test :refer [deftest is]]))

(def fifteen 14)

(deftest !-test
  (sut/! '{$$ [clojure.test/run-tests
               com.gfredericks.dot-slash-2-test/fifteen]
           && [clojure.core/+
               clojure.repl/doc]})
  (is (resolve '$$/run-tests))
  (is (resolve '$$/fifteen))
  (is (= 14 @(resolve '$$/fifteen)))
  (is (resolve '&&/+))
  (is (= 19 ((resolve '&&/+)
             @(resolve '$$/fifteen)
             5)))
  (is (thrown? Exception (eval '&&/doc)))

  (is (re-find #"Returns the sum of nums"
               (eval
                '(:doc (meta #'&&/+))))))

(deftest require-failure-test
  (let [stderr
        (with-out-str
          (binding [*err* *out*]
            (sut/! '{&$%&! [doesn't-exist/at-all
                            com.gfredericks.dot-slash-2-test/fifteen]})))]
    (is (not (resolve '&$%&!/at-all))
        "The proxy var didn't get created")
    (is (re-find #"dot-slash-2 failed to require 'doesn't-exist to proxy 'at-all" stderr))
    (is (= 14 (eval '&$%&!/fifteen)))))

(def mutate-me 49)

(deftest update-test
  (sut/! '{!!! [com.gfredericks.dot-slash-2-test/mutate-me]})
  (is (= 49 (eval '!!!/mutate-me)))
  (alter-var-root #'mutate-me inc)
  (is (= 50 (eval '!!!/mutate-me))))
