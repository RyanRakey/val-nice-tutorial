(ns val-nice-tutorial.listener
  (:require [val-nice-tutorial.robot :as robot])
  (:import (com.github.kwhat.jnativehook GlobalScreen)
           (com.github.kwhat.jnativehook.keyboard NativeKeyListener NativeKeyEvent)))

(defn create-listener
  [config]
  (reify NativeKeyListener
    (nativeKeyPressed [_ e]
      (when (and (= (.getKeyCode e) (:key-code config))
                 (= (.getKeyLocation e) (:key-location config)))
        (println (str "Sending '" (:message config) "' to "
                      (name (:chat-mode config)) " chat..."))
        (robot/send-message config)))
    (nativeKeyReleased [_ _])
    (nativeKeyTyped [_ _])))

(defn add-listener! [l] (GlobalScreen/addNativeKeyListener l))
(defn remove-listener! [l] (GlobalScreen/removeNativeKeyListener l))

(defn register-hook! [] (GlobalScreen/registerNativeHook))
(defn unregister-hook! [] (GlobalScreen/unregisterNativeHook))
