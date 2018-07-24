# dot-slash-2

`dot-slash-2` is a library version of
[lein-shorthand](https://github.com/palletops/lein-shorthand), which
is a leiningen plugin.

It lets you create namespaces with proxies to other namespaces. It is
named after the idea of creating a namespace called `.` with dev
utilities, to allow accessing them with the syntax `./foo` from
anywhere without needing to require anything.

## Obtention

`[com.gfredericks/dot-slash-2 "0.1.5"]`

## Usage

### Raw

#### Basic

``` clojure
(require '[com.gfredericks.dot-slash-2 :as dot-slash-2])

;; Defines a namespace called . and adds ./doc, ./source,
;; ./run-tests, and ./refresh
(dot-slash-2/!
 '{. [clojure.repl/doc
      clojure.repl/source
      clojure.test/run-tests
      clojure.tools.namespace.repl/refresh]})
```

#### Advanced

``` clojure
(require '[com.gfredericks.dot-slash-2 :as dot-slash-2])

(dot-slash-2/!
 '{& [{:var clojure.repl/doc
       ;; :name allows renaming the alias
       :name d}

      {:var user/cool-util
       ;; dynamic? means the var will be looked up on each call, which
       ;; supports namespace reloading
       :dynamic? true}

      ;; the map form can be mixed with the simple form
      clojure.data/diff

      {:var    clojure.core/and
       ;; lazy? means the underlying namespace will not be required
       ;; until the first call
       :lazy?  true
       ;; when using lazy? you have to indicate if it is a macro or
       ;; not
       :macro? true}]})
```

### Leiningen

In your `project.clj` or `:user` profile or whatever:

``` clojure
:injections [(do
               (require 'com.gfredericks.dot-slash-2)
               ((resolve 'com.gfredericks.dot-slash-2/!)
                '{. [clojure.repl/doc
                     clojure.repl/source
                     clojure.test/run-tests
                     clojure.tools.namespace.repl/refresh]}))]
```

## License

Copyright © 2017 Gary Fredericks

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
