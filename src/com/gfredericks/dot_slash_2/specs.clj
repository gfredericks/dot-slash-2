(ns com.gfredericks.dot-slash-2.specs
  (:require
   [clojure.spec.alpha :as s]))

(s/def :com.gfredericks.dot-slash-2/spec
  (s/map-of simple-symbol?
            (s/coll-of :com.gfredericks.dot-slash-2/var-cfg)))

(s/def :com.gfredericks.dot-slash-2/var-cfg
  (s/or :var-only :com.gfredericks.dot-slash-2/var
        :map (s/keys :req-un [:com.gfredericks.dot-slash-2/var]
                     :opt-un [:com.gfredericks.dot-slash-2/dynamic?
                              :com.gfredericks.dot-slash-2/name])))

(s/def :com.gfredericks.dot-slash-2/var qualified-symbol?)
(s/def :com.gfredericks.dot-slash-2/dynamic? boolean?)
(s/def :com.gfredericks.dot-slash-2/name simple-symbol?)
