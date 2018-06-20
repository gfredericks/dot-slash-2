(ns com.gfredericks.dot-slash-2)

(try
  (require '[clojure.spec.alpha :as s])
  (require 'com.gfredericks.dot-slash-2.specs)
  (eval
   '(do
      (defn valid? [spec] (s/valid? ::spec spec))
      (defn explain [spec] (s/explain ::spec spec))))
  (catch Exception e
    (eval
     '(do
        (defn valid? [spec] true)
        (defn explain [spec]
          (assert false "unreachable"))))))

(defn ^:private normalize
  [sym-or-map]
  (if (symbol? sym-or-map)
    {:var sym-or-map}
    sym-or-map))

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
  [spec]
  (if (valid? spec)
    (doseq [[ns-name vars-and-things] spec]
      (create-ns ns-name)
      (doseq [sym-or-map vars-and-things
              :let [{sym        :var
                     proxy-name :name
                     :keys      [dynamic?]}
                    (normalize sym-or-map)

                    its-ns (symbol (namespace sym))

                    proxy-name (or proxy-name (symbol (name sym)))]]
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
         (if-let [^clojure.lang.Var orig-var (resolve sym)]
           (if dynamic?
             (doto
                 (intern ns-name
                         proxy-name
                         (fn [& args]
                           (if-let [f (resolve sym)]
                             (apply f args)
                             (throw (Exception.
                                     (format "dot-slash-2 proxy failed on (resolve '%s)"
                                             sym))))))
               (alter-meta! assoc
                            :doc (format "Proxy to %s\n\nOriginal docs:\n\n%s\n%s"
                                         sym
                                         (pr-str (:arglists (meta orig-var)))
                                         (:doc (meta orig-var)))))
             (let [^clojure.lang.Var new-var (intern ns-name
                                                     proxy-name
                                                     @orig-var)]
               (doto
                   new-var
                 (cond-> (.isMacro orig-var) (.setMacro))
                 (alter-meta! merge (meta orig-var)))
               (add-watch orig-var (gensym "dot-slash-2")
                          (fn [_ _ _ new]
                            (alter-var-root new-var (constantly new))
                            (alter-meta! new-var merge (meta orig-var))))))
           (binding [*out* *err*]
             (printf "dot-slash-2 failed to resolve %s\n" sym))))))
    (binding [*out* *err*]
      (println "Bad arg to dot-slash-2/!:")
      (explain spec))))
