# dot-slash-2

`dot-slash-2` is a library version of
[lein-shorthand](https://github.com/palletops/lein-shorthand), which
is a leiningen plugin.

It lets you create namespaces with proxies to other namespaces. It is
named after the idea of creating a namespace called `.` with dev
utilities, to allow accessing them with the syntax `./foo` from
anywhere without needing to require anything.

## Obtention

`[com.gfredericks/dot-slash-2 "0.1.1"]`

## Usage

### Raw

```
(require '[com.gfredericks.dot-slash-2 :as dot-slash-2])

;; Defines a namespace called . and adds ./doc, ./source,
;; ./run-tests, and ./refresh
(dot-slash-2/!
 '{. [clojure.repl/doc
      clojure.repl/source
      clojure.test/run-tests
      clojure.tools.namespace.repl/refresh]})
```

### Leiningen

In your `project.clj` or `:user` profile or whatever:

```
:injections [(do
               (require 'com.gfredericks.dot-slash-2)
               ((resolve 'com.gfredericks.dot-slash-2/!)
                '{. [clojure.repl/doc
                     clojure.repl/source
                     clojure.test/run-tests
                     clojure.tools.namespace.repl/refresh]}))]
```

## Potential Future Features

- Renaming
- Lazy loading
- Dynamic resolution, for proxying to code that can be reloaded

## License

Copyright © 2017 Gary Fredericks

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
