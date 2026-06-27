(ns val-nice-tutorial.core
  (:require [val-nice-tutorial.config :as config]
            [val-nice-tutorial.tray :as tray]
            [val-nice-tutorial.settings :as settings])
  (:import (com.github.kwhat.jnativehook GlobalScreen NativeHookException)
           (com.github.kwhat.jnativehook.keyboard NativeKeyListener NativeKeyEvent)
           (java.awt Robot)
           (java.awt.event KeyEvent))
  (:gen-class))

(defn char->key-code
  [c]
  (case c
    \space KeyEvent/VK_SPACE
    \/     KeyEvent/VK_SLASH
    (Character/toUpperCase (int c))))

(defn press-char!
  [robot c]
  (let [key-code (char->key-code c)]
    (if (Character/isUpperCase c)
      (do (.keyPress robot KeyEvent/VK_SHIFT)
          (.keyPress robot key-code)
          (.keyRelease robot key-code)
          (.keyRelease robot KeyEvent/VK_SHIFT))
      (do (.keyPress robot key-code)
          (.keyRelease robot key-code)))))

(defn type-text!
  [robot text]
  (doseq [c text]
    (press-char! robot c)
    (.delay robot 15)))

(defn press-enter!
  [robot]
  (.keyPress robot KeyEvent/VK_ENTER)
  (.keyRelease robot KeyEvent/VK_ENTER))

(defn create-robot [] (Robot.))

(defonce app-state
  (atom {:running? false
         :listener nil
         :config {:key-code NativeKeyEvent/VC_1
                  :key-location NativeKeyEvent/KEY_LOCATION_NUMPAD
                  :message "nice tutorial"
                  :chat-mode :all}}))

(defn message-prefix
  [chat-mode]
  (if (= :team chat-mode) "/team " "/all "))

(defn send-message
  ([] (send-message (create-robot)))
  ([robot]
   (let [config (:config @app-state)]
     (.delay robot 200)
     (press-enter! robot)
     (.delay robot 150)
     (type-text! robot (message-prefix (:chat-mode config)))
     (.delay robot 50)
     (type-text! robot (:message config))
     (.delay robot 100)
     (press-enter! robot))))

(defn create-listener
  [state-atom]
  (let [send-fn (fn []
                  (let [cfg (:config @state-atom)]
                    (println (str "Sending '" (:message cfg) "' to "
                                  (name (:chat-mode cfg)) " chat..."))
                    (send-message)))]
    (reify NativeKeyListener
      (nativeKeyPressed [_ e]
        (let [config (:config @state-atom)]
          (when (and (= (.getKeyCode e) (:key-code config))
                     (= (.getKeyLocation e) (:key-location config)))
            (send-fn))))
      (nativeKeyReleased [_ _])
      (nativeKeyTyped [_ _]))))

(defn add-listener! [l] (GlobalScreen/addNativeKeyListener l))
(defn remove-listener! [l] (GlobalScreen/removeNativeKeyListener l))

(defn start!
  []
  (when-not (:running? @app-state)
    (let [l (create-listener app-state)]
      (add-listener! l)
      (swap! app-state assoc :listener l :running? true)
      (println "Listener started."))))

(defn stop!
  []
  (when (:running? @app-state)
    (when-let [l (:listener @app-state)]
      (remove-listener! l))
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
    (GlobalScreen/registerNativeHook)
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
      (GlobalScreen/unregisterNativeHook))))
