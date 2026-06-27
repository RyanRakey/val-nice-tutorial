(ns val-nice-tutorial.ui-interop-test
  (:require [clojure.test :refer [deftest is testing]])
  (:import (java.awt Font)
           (javax.swing JDialog JLabel)))

(deftest derive-font-with-float-test
  (testing "deriveFont with BOLD (int) and float size succeeds"
    (let [f (.deriveFont (Font. "SansSerif" Font/PLAIN 12) Font/BOLD (float 14))]
      (is (instance? Font f))
      (is (= Font/BOLD (.getStyle f)))
      (is (== 14.0 (.getSize f)))))
  (testing "deriveFont with Long size throws"
    (is (thrown? IllegalArgumentException
                 (.deriveFont (Font. "SansSerif" Font/PLAIN 12) Font/BOLD 14))))
  (testing "deriveFont with Integer size throws"
    (is (thrown? IllegalArgumentException
                 (.deriveFont (Font. "SansSerif" Font/PLAIN 12) Font/BOLD (int 14)))))
  (testing "deriveFont on JLabel default font with BOLD and float size succeeds"
    (let [f (.deriveFont (.getFont (JLabel.)) Font/BOLD (float 14))]
      (is (instance? Font f)))))

(deftest set-location-relative-to-test
  (testing "setLocationRelativeTo with nil centers the dialog"
    (let [d (JDialog.)]
      (try
        (.setLocationRelativeTo d nil)
        (is true "setLocationRelativeTo completed without exception")
        (finally (.dispose d))))))
