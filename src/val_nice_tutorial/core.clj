(ns val-nice-tutorial.core
  (:require [val-nice-tutorial.config :as config]
            [val-nice-tutorial.robot :as robot]
            [val-nice-tutorial.listener :as listener]
            [val-nice-tutorial.tray :as tray]
            [val-nice-tutorial.settings :as settings])
  (:import (com.github.kwhat.jnativehook NativeHookException))
  (:gen-class))

(defonce app-state
  (atom {:running? false
         :listener nil
         :config config/default-config}))

(defn start!
  []
  (when-not (:running? @app-state)
    (let [l (listener/create-listener (:config @app-state))]
      (listener/add-listener! l)
      (swap! app-state assoc :listener l :running? true)
      (println "Listener started."))))

(defn stop!
  []
  (when (:running? @app-state)
    (when-let [l (:listener @app-state)]
      (listener/remove-listener! l))
    (swap! app-state assoc :listener nil :running? false)
    (println "Listener stopped.")))

(defn restart!
  []
  (stop!)
  (start!))

(defn -main
  [& _args]
  (let [saved-config (config/load-config)]
    (swap! app-state assoc :config saved-config))
  (println "Valorant Nice Tutorial utility started.")
  (try
    (listener/register-hook!)
    (start!)
    (tray/setup-tray! app-state
                      start! stop!
                      (fn [update-fn]
                        (settings/show-settings app-state
                                                start! stop! restart!
                                                update-fn)))
    @(promise)
    (catch NativeHookException e
      (println "Failed to register native hook:" (.getMessage e))
      (System/exit 1))
    (finally
      (stop!)
      (listener/unregister-hook!))))
