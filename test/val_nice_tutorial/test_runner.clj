(ns val-nice-tutorial.test-runner
  (:require [clojure.test :as t]
            [val-nice-tutorial.core-test]
            [val-nice-tutorial.ui-interop-test]))

(defn -main []
  (t/run-tests 'val-nice-tutorial.core-test
               'val-nice-tutorial.ui-interop-test))
