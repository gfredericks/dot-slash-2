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
      (and
       (try
         (require its-ns)
         true
         (catch Exception e
           (binding [*out* *err*]
             (printf "dot-slash-2 failed to require '%s to proxy '%s\n  (%s)\n"
                     its-ns (name sym) (.getMessage e)))
           ;; short-circuits the `and` so the rest of the
           ;; code doesn't execute
           false))
       (let [the-var (resolve sym)]
         (doto
             (intern ns-name (symbol (name sym))
                     @the-var)
           (cond-> (.isMacro the-var) (.setMacro))
           (alter-meta! merge (meta the-var))))))))
