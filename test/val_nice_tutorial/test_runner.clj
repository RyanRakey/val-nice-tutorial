(ns val-nice-tutorial.test-runner
  (:require [clojure.test :as t]
            [val-nice-tutorial.core-test]))

(defn -main []
  (t/run-tests 'val-nice-tutorial.core-test))
