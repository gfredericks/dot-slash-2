(ns com.gfredericks.dot-slash-2.specs
  (:require
   [clojure.spec.alpha :as s]))

(s/def :com.gfredericks.dot-slash-2/spec
  (s/map-of simple-symbol?
            (s/coll-of :com.gfredericks.dot-slash-2/var-cfg)))

(defn lazy-specifies-macro?
  [m]
  (if (:com.gfredericks.dot-slash-2/lazy? m)
    (contains? m :com.gfredericks.dot-slash-2/macro?)
    true))

(s/def :com.gfredericks.dot-slash-2/var-cfg
  (s/or :var-only :com.gfredericks.dot-slash-2/var
        :map (s/and (s/keys :req-un [:com.gfredericks.dot-slash-2/var]
                            :opt-un [:com.gfredericks.dot-slash-2/dynamic?
                                     :com.gfredericks.dot-slash-2/lazy?
                                     :com.gfredericks.dot-slash-2/macro?
                                     :com.gfredericks.dot-slash-2/name])
                    lazy-specifies-macro?)))

(s/def :com.gfredericks.dot-slash-2/var qualified-symbol?)
(s/def :com.gfredericks.dot-slash-2/dynamic? boolean?)
(s/def :com.gfredericks.dot-slash-2/lazy? boolean?)
(s/def :com.gfredericks.dot-slash-2/macro? boolean?)
(s/def :com.gfredericks.dot-slash-2/name simple-symbol?)
