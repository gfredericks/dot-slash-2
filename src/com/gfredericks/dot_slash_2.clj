(ns com.gfredericks.dot-slash-2)

(defn !
  "Defines namespaces and proxy vars based on the given spec.

  E.g.:

    '{. [clojure.test/run-tests
         clojure.repl/doc]}

  will create (if necessary) a namespace called ., containing
  a function called run-tests and a macro called doc."
  ;; TODO:
  ;; - renaming
  ;; - laziness
  ;; - dynamism (in case a namespace gets reloaded)
  [spec]
  (doseq [[ns-name vars-and-things] spec]
    (create-ns ns-name)
    (doseq [sym vars-and-things
            :let [its-ns (symbol (namespace sym))]]
      (require its-ns)
      (let [the-var (resolve sym)]
        (doto
            (intern ns-name (symbol (name sym))
                    @the-var)
          (cond-> (.isMacro the-var) (.setMacro)))))))
