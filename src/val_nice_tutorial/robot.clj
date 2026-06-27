(ns val-nice-tutorial.robot
  (:import (java.awt Robot)
           (java.awt.event KeyEvent)))

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

(defn message-prefix
  [chat-mode]
  (case chat-mode :team "/team " "/all "))

(defn send-message
  ([config] (send-message (create-robot) config))
  ([robot {:keys [chat-mode message]}]
   (.delay robot 200)
   (press-enter! robot)
   (.delay robot 150)
   (type-text! robot (message-prefix chat-mode))
   (.delay robot 50)
   (type-text! robot message)
   (.delay robot 100)
   (press-enter! robot)))
