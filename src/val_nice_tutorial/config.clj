(ns val-nice-tutorial.config
  (:require [clojure.edn :as edn])
  (:import (java.io File))
  (:import (com.github.kwhat.jnativehook.keyboard NativeKeyEvent)))

(def config-dir
  (str (System/getProperty "user.home") "/.val-nice-tutorial"))

(def config-file
  (str config-dir "/config.edn"))

(def default-config
  {:key-code NativeKeyEvent/VC_1
   :key-location NativeKeyEvent/KEY_LOCATION_NUMPAD
   :message "nice tutorial"
   :chat-mode :all})

(defn load-config
  []
  (let [f (File. config-file)]
    (if (.exists f)
      (try (edn/read-string (slurp f))
           (catch Exception _ default-config))
      default-config)))

(defn message-prefix
  [chat-mode]
  (case chat-mode :team "/team " "/all "))

(defn save-config
  [config]
  (let [f (File. config-file)
        d (File. config-dir)]
    (.mkdirs d)
    (spit f (with-out-str (pr config)))))
