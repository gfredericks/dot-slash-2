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
  (is (thrown? Exception (eval '&&/doc))))
