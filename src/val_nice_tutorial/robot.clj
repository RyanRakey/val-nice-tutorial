(ns val-nice-tutorial.robot
  (:require [val-nice-tutorial.config :as config])
  (:import (java.awt Robot)
           (java.awt.event KeyEvent)))

(def initial-chat-delay-ms 200)
(def chat-activation-delay-ms 150)
(def prefix-typing-delay-ms 50)
(def char-typing-delay-ms 15)
(def send-delay-ms 100)

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
    (.delay robot char-typing-delay-ms)))

(defn press-enter!
  [robot]
  (.keyPress robot KeyEvent/VK_ENTER)
  (.keyRelease robot KeyEvent/VK_ENTER))

(defn create-robot [] (Robot.))

(defn send-message
  ([config] (send-message (create-robot) config))
  ([robot {:keys [chat-mode message]}]
   (.delay robot initial-chat-delay-ms)
   (press-enter! robot)
   (.delay robot chat-activation-delay-ms)
   (type-text! robot (config/message-prefix chat-mode))
   (.delay robot prefix-typing-delay-ms)
   (type-text! robot message)
   (.delay robot send-delay-ms)
   (press-enter! robot)))
