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

(defn ^:private require-and-resolve
  [sym]
  (let [ns-sym (symbol (namespace sym))]
    (try
      (require ns-sym)
      (catch Exception e
        (throw (Exception. (format "dot-slash-2 failed to require %s"
                                   ns-sym)
                           e))))
    (or (resolve sym)
        (throw (Exception. (format "dot-slash-2 failed to resolve %s"
                                   sym))))))

(defn ^:private sync-changes
  [proxy-var underlying-var]
  (add-watch underlying-var (gensym "dot-slash-2")
             (fn [_ _ _ new]
               (alter-var-root proxy-var (constantly new))
               (alter-meta! proxy-var merge (meta underlying-var)))))

(defn ^:private set-dynamic-docstring
  [proxy-var underlying-var]
  (alter-meta! proxy-var assoc :doc
               (format "Proxy to %s\n\nOriginal docs:\n\n%s\n%s"
                       underlying-var
                       (pr-str (:arglists (meta underlying-var)))
                       (:doc (meta underlying-var)))))

(defn ^:private setup-var
  [underlying-symbol new-var dynamic? lazy? macro?]
  (if lazy?
    ;; Note: a consequence of how this is done independently for each
    ;; var is that docstrings and other metadata aren't applied until
    ;; the first call, even if they're already available (e.g., if the
    ;; namespace is already loaded for some independent reason, or
    ;; even because of another call to a dot-slash var); we could at
    ;; least improve it to where we
    ;;
    ;; 1) check at this point if the namespace is already loaded,
    ;;    and ignore the laziness if so
    ;; 2) when we process the first call to a var, realize all the
    ;;    other vars proxying to that namespace as well; we might
    ;;    even take the opportunity to check if any other namespaces
    ;;    have been loaded in the meantime
    (let [root-value
          (if dynamic?
            (let [d (delay (set-dynamic-docstring
                            new-var
                            (require-and-resolve underlying-symbol)))]
              (fn dynamic-var-proxy [& args]
                (force d)
                (apply (require-and-resolve underlying-symbol) args)))
            (fn one-time-lazy-stub [& args]
              (let [underlying-var (require-and-resolve underlying-symbol)]
                (doto new-var
                  (alter-var-root (constantly @underlying-var))
                  (alter-meta! merge (meta underlying-var))
                  (sync-changes underlying-var))
                (apply @new-var args))))]
      (doto new-var
        (alter-var-root (constantly root-value))
        (cond-> macro? .setMacro)))
    (let [underlying-var (delay (require-and-resolve underlying-symbol))]
      (and
       (try
         (deref underlying-var)
         (catch Exception e
           (binding [*out* *err*]
             (printf "WARNING: %s\n(calls to %s will fail)\n"
                     (.getMessage e)
                     new-var))
           (alter-var-root new-var
                           (constantly
                            (fn ex-rethrower [& args] (deref underlying-var))))
           false))
       (let [underlying-var @underlying-var
             root-value (if dynamic?
                          (fn [& args]
                            (apply (require-and-resolve underlying-symbol) args))
                          @underlying-var)]
         (doto new-var
           (alter-var-root (constantly root-value))
           (alter-meta! merge (meta underlying-var))
           (cond-> (.isMacro underlying-var) .setMacro)
           (cond-> dynamic? (set-dynamic-docstring underlying-var))
           (cond-> (not dynamic?) (sync-changes underlying-var))))))))

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
                     :keys      [dynamic? lazy? macro?]}
                    (normalize sym-or-map)

                    its-ns (symbol (namespace sym))

                    proxy-name (or proxy-name (symbol (name sym)))
                    new-var (intern ns-name proxy-name)]]
        (setup-var sym new-var dynamic? lazy? macro?)))
    (binding [*out* *err*]
      (println "Bad arg to dot-slash-2/!:")
      (explain spec))))
