(ns com.gfredericks.dot-slash-2-test
  (:require [clojure.java.shell :refer [sh]]
            [com.gfredericks.dot-slash-2 :as sut]
            [clojure.test :refer [deftest is]]))

(def fifteen 14)

(deftest !-test
  (sut/! '{$$ [clojure.test/run-tests
               com.gfredericks.dot-slash-2-test/fifteen]
           && [clojure.core/+
               clojure.repl/doc
               {:var clojure.repl/apropos
                :name tompkins}
               {:var com.gfredericks.dot-slash-2-test/fifteen
                :name thirteen}]})
  (is (resolve '$$/run-tests))
  (is (resolve '$$/fifteen))
  (is (= 14 @(resolve '$$/fifteen)))
  (is (resolve '&&/+))
  (is (resolve '&&/tompkins))
  (is (= 14 @(resolve '&&/thirteen)))
  (is (not (resolve '&&/apropos)))
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
    (is (resolve '&$%&!/at-all)
        "The proxy var was created")
    (is (thrown-with-msg? Exception #"failed to require"
                          (eval '(&$%&!/at-all 1 2 3)))
        "throws an exception if you call it")
    (is (re-find #"dot-slash-2 failed to require doesn't-exist" stderr))
    (is (= 14 (eval '&$%&!/fifteen)))))

(def mutate-me 49)

(deftest update-test
  (sut/! '{!!! [com.gfredericks.dot-slash-2-test/mutate-me]})
  (is (= 49 (eval '!!!/mutate-me)))
  (alter-var-root #'mutate-me inc)
  (is (= 50 (eval '!!!/mutate-me))))

(defn fake-require-new-ns
  [sym]
  (create-ns sym)
  (dosync (alter @#'clojure.core/*loaded-libs* conj sym)))

(deftest dynamism-test
  (fake-require-new-ns 'ns-145)
  (intern 'ns-145 'foo-static  (fn [] :old-value))
  (intern 'ns-145 'foo-dynamic (fn [] :old-value))
  (sut/! '{yopep [ns-145/foo-static
                  {:var ns-145/foo-dynamic
                   :dynamic? true}]})
  (is (= :old-value (eval '(yopep/foo-static))))
  (is (= :old-value (eval '(yopep/foo-dynamic))))
  ;; Simulating what happens with, e.g.,
  ;; clojure.tools.namespace.repl/refresh
  (remove-ns 'ns-145)
  (fake-require-new-ns 'ns-145)
  (intern 'ns-145 'foo-static  (fn [] :new-value))
  (intern 'ns-145 'foo-dynamic (fn [] :new-value))
  (is (= :old-value (eval '(yopep/foo-static))))
  (is (= :new-value (eval '(yopep/foo-dynamic)))))

(deftest bad-name-test
  (let [stderr
        (with-out-str
          (binding [*err* *out*]
            (sut/! '{x2378 [clojure.test/ns-exists-but-var-doesn't]})))]
    (is (re-find #"dot-slash-2 .*clojure.test/ns-exists-but-var-doesn't" stderr))))

(defmacro unnecessary-adding-macro
  [a b]
  `(+ ~a ~b))

(deftest dynamic-macros-test
  (sut/! {'ns1894 [{:var      `unnecessary-adding-macro
                    :dynamic? true}]})
  (is (= 14 (eval '(ns1894/unnecessary-adding-macro 8 6)))))

(deftest lazy-test
  (let [ns-name (str "ns" (rand-int 0x7FFFFFFF))
        fn-sym (symbol ns-name "jamboreen")]
    (sh "mkdir" "tmp-test-classpath")
    (spit (format "tmp-test-classpath/%s.clj" ns-name)
          (format "(ns %s) (defn jamboreen [x] x)"
                  ns-name))
    (sut/! {'ns82 [{:var      fn-sym
                    :dynamic? true
                    :lazy?    true
                    :macro?   false}
                   {:var      'clojure.data/diff
                    :lazy?    true
                    :macro?   false}]})
    (is (thrown? Exception
                 (eval (list fn-sym :foo)))
        "hasn't been required yet")
    (is (= :foo (eval '(ns82/jamboreen :foo)))
        "works through the proxy, which requires it")
    (is (= :foo (eval (list fn-sym :foo)))
        "direct call works this time, proving it's been required")
    (eval '(ns82/diff {} {}))
    (is (re-find #"Recursively compares a and b"
                 (-> 'ns82/diff resolve meta :doc))
        "adds the docstring once it's been called")))

(deftest lazy-dynamic-test
  (sut/! {'ns4781 [{:var      'clojure.data/diff
                    :dynamic? true
                    :lazy?    true
                    :macro?   false}
                   {:var      'clojure.core/or
                    :dynamic? true
                    :lazy?    true
                    :macro?   true}]})
  (is (= [nil nil {}] (eval '(ns4781/diff {} {}))))
  (is (= 42 (eval '(ns4781/or nil false false 42 (throw (Exception. "unreachable"))))))
  (is (re-find #"Recursively compares a and b"
               (-> 'ns4781/diff resolve meta :doc))
      "adds the docstring once it's been called"))
