(ns val-nice-tutorial.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [val-nice-tutorial.core :as sut])
  (:import (com.github.kwhat.jnativehook.keyboard NativeKeyEvent)
           (java.awt.event KeyEvent)
           (java.awt Robot AWTException)))

(defn recording-robot
  [calls]
  (try
    (proxy [Robot] []
      (keyPress [code] (swap! calls conj [:key-press code]))
      (keyRelease [code] (swap! calls conj [:key-release code]))
      (delay [ms] (swap! calls conj [:delay ms])))
    (catch AWTException e
      (throw (RuntimeException. "Cannot create mock Robot: " e)))))

(defn with-config
  [config f]
  (let [orig @sut/app-state]
    (swap! sut/app-state assoc :config config)
    (try (f)
         (finally (reset! sut/app-state orig)))))

(def default-test-config
  {:key-code NativeKeyEvent/VC_1
   :key-location NativeKeyEvent/KEY_LOCATION_NUMPAD
   :message "nice tutorial"
   :chat-mode :all})

(deftest char->key-code-test
  (testing "letters map to uppercase AWT key codes"
    (is (= KeyEvent/VK_A (sut/char->key-code \a)))
    (is (= KeyEvent/VK_Z (sut/char->key-code \z)))
    (is (= KeyEvent/VK_M (sut/char->key-code \m))))
  (testing "space maps to VK_SPACE"
    (is (= KeyEvent/VK_SPACE (sut/char->key-code \space))))
  (testing "slash maps to VK_SLASH"
    (is (= KeyEvent/VK_SLASH (sut/char->key-code \/))))
  (testing "case fallthrough for non-special chars"
    (is (= (Character/toUpperCase (int \1)) (sut/char->key-code \1)))
    (is (= (Character/toUpperCase (int \.)) (sut/char->key-code \.)))))

(deftest press-char!-test
  (testing "lowercase letter presses and releases its uppercase key code"
    (let [calls (atom []) robot (recording-robot calls)]
      (sut/press-char! robot \a)
      (is (= [[:key-press KeyEvent/VK_A] [:key-release KeyEvent/VK_A]] @calls))))
  (testing "space presses and releases VK_SPACE"
    (let [calls (atom []) robot (recording-robot calls)]
      (sut/press-char! robot \space)
      (is (= [[:key-press KeyEvent/VK_SPACE] [:key-release KeyEvent/VK_SPACE]] @calls))))
  (testing "slash presses and releases VK_SLASH"
    (let [calls (atom []) robot (recording-robot calls)]
      (sut/press-char! robot \/)
      (is (= [[:key-press KeyEvent/VK_SLASH] [:key-release KeyEvent/VK_SLASH]] @calls))))
  (testing "uppercase letter wraps in shift"
    (let [calls (atom []) robot (recording-robot calls)]
      (sut/press-char! robot \A)
      (is (= [[:key-press KeyEvent/VK_SHIFT]
              [:key-press KeyEvent/VK_A]
              [:key-release KeyEvent/VK_A]
              [:key-release KeyEvent/VK_SHIFT]] @calls)))))

(deftest type-text!-test
  (testing "types each character with interleaving delays"
    (let [calls (atom []) robot (recording-robot calls)]
      (sut/type-text! robot "ab")
      (is (= [[:key-press KeyEvent/VK_A] [:key-release KeyEvent/VK_A] [:delay 15]
              [:key-press KeyEvent/VK_B] [:key-release KeyEvent/VK_B] [:delay 15]]
             @calls))))
  (testing "types \"/all \" correctly"
    (let [calls (atom []) robot (recording-robot calls)]
      (sut/type-text! robot "/all ")
      (is (= [[:key-press KeyEvent/VK_SLASH] [:key-release KeyEvent/VK_SLASH] [:delay 15]
              [:key-press KeyEvent/VK_A] [:key-release KeyEvent/VK_A] [:delay 15]
              [:key-press KeyEvent/VK_L] [:key-release KeyEvent/VK_L] [:delay 15]
              [:key-press KeyEvent/VK_L] [:key-release KeyEvent/VK_L] [:delay 15]
              [:key-press KeyEvent/VK_SPACE] [:key-release KeyEvent/VK_SPACE] [:delay 15]]
             @calls)))))

(deftest press-enter!-test
  (testing "presses and releases VK_ENTER"
    (let [calls (atom []) robot (recording-robot calls)]
      (sut/press-enter! robot)
      (is (= [[:key-press KeyEvent/VK_ENTER] [:key-release KeyEvent/VK_ENTER]] @calls)))))

(deftest message-prefix-test
  (testing "all chat prefix is \"/all \""
    (is (= "/all " (sut/message-prefix :all))))
  (testing "team chat prefix is \"/team \""
    (is (= "/team " (sut/message-prefix :team)))))

(deftest send-message-test
  (testing "sends \"/all nice tutorial\" with default config"
    (with-config default-test-config
      (fn []
        (let [calls (atom []) robot (recording-robot calls)]
          (sut/send-message robot)
          (is (= [[:delay 200]
                  [:key-press KeyEvent/VK_ENTER] [:key-release KeyEvent/VK_ENTER]
                  [:delay 150]
                  [:key-press KeyEvent/VK_SLASH] [:key-release KeyEvent/VK_SLASH] [:delay 15]
                  [:key-press KeyEvent/VK_A] [:key-release KeyEvent/VK_A] [:delay 15]
                  [:key-press KeyEvent/VK_L] [:key-release KeyEvent/VK_L] [:delay 15]
                  [:key-press KeyEvent/VK_L] [:key-release KeyEvent/VK_L] [:delay 15]
                  [:key-press KeyEvent/VK_SPACE] [:key-release KeyEvent/VK_SPACE] [:delay 15]
                  [:delay 50]
                  [:key-press KeyEvent/VK_N] [:key-release KeyEvent/VK_N] [:delay 15]
                  [:key-press KeyEvent/VK_I] [:key-release KeyEvent/VK_I] [:delay 15]
                  [:key-press KeyEvent/VK_C] [:key-release KeyEvent/VK_C] [:delay 15]
                  [:key-press KeyEvent/VK_E] [:key-release KeyEvent/VK_E] [:delay 15]
                  [:key-press KeyEvent/VK_SPACE] [:key-release KeyEvent/VK_SPACE] [:delay 15]
                  [:key-press KeyEvent/VK_T] [:key-release KeyEvent/VK_T] [:delay 15]
                  [:key-press KeyEvent/VK_U] [:key-release KeyEvent/VK_U] [:delay 15]
                  [:key-press KeyEvent/VK_T] [:key-release KeyEvent/VK_T] [:delay 15]
                  [:key-press KeyEvent/VK_O] [:key-release KeyEvent/VK_O] [:delay 15]
                  [:key-press KeyEvent/VK_R] [:key-release KeyEvent/VK_R] [:delay 15]
                  [:key-press KeyEvent/VK_I] [:key-release KeyEvent/VK_I] [:delay 15]
                  [:key-press KeyEvent/VK_A] [:key-release KeyEvent/VK_A] [:delay 15]
                  [:key-press KeyEvent/VK_L] [:key-release KeyEvent/VK_L] [:delay 15]
                  [:delay 100]
                  [:key-press KeyEvent/VK_ENTER] [:key-release KeyEvent/VK_ENTER]]
                 @calls))))))
  (testing "sends \"/team nice tutorial\" with team chat config"
    (with-config (assoc default-test-config :chat-mode :team)
      (fn []
        (let [calls (atom []) robot (recording-robot calls)]
          (sut/send-message robot)
          (is (= [[:key-press KeyEvent/VK_T] [:key-release KeyEvent/VK_T] [:delay 15]
                  [:key-press KeyEvent/VK_E] [:key-release KeyEvent/VK_E] [:delay 15]
                  [:key-press KeyEvent/VK_A] [:key-release KeyEvent/VK_A] [:delay 15]
                  [:key-press KeyEvent/VK_M] [:key-release KeyEvent/VK_M] [:delay 15]
                  [:key-press KeyEvent/VK_SPACE] [:key-release KeyEvent/VK_SPACE] [:delay 15]]
                 (->> @calls (drop 7) (take 15))))))))
  (testing "sends custom message"
    (with-config (assoc default-test-config :message "gg wp")
      (fn []
        (let [calls (atom []) robot (recording-robot calls)]
          (sut/send-message robot)
          (let [msg-events (->> @calls (drop 20) (take 15))]
            (is (= [[:key-press KeyEvent/VK_G] [:key-release KeyEvent/VK_G] [:delay 15]
                    [:key-press KeyEvent/VK_G] [:key-release KeyEvent/VK_G] [:delay 15]
                    [:key-press KeyEvent/VK_SPACE] [:key-release KeyEvent/VK_SPACE] [:delay 15]
                    [:key-press KeyEvent/VK_W] [:key-release KeyEvent/VK_W] [:delay 15]
                    [:key-press KeyEvent/VK_P] [:key-release KeyEvent/VK_P] [:delay 15]]
                   msg-events)))))))
  (testing "uses create-robot when called without args"
    (let [calls (atom []) called (atom false)]
      (with-redefs [sut/create-robot (fn [] (reset! called true) (recording-robot calls))]
        (with-config default-test-config (fn [] (sut/send-message))))
      (is called))))

(defn make-num-event
  [key-code location]
  (NativeKeyEvent. NativeKeyEvent/NATIVE_KEY_PRESSED 0 0 key-code NativeKeyEvent/CHAR_UNDEFINED location))

(deftest listener-dispatch-test
  (testing "triggers on matching numpad 1 event"
    (let [sent? (atom false)
          listener (sut/create-listener (atom {:config default-test-config}))
          event (make-num-event NativeKeyEvent/VC_1 NativeKeyEvent/KEY_LOCATION_NUMPAD)]
      (with-redefs [sut/send-message (fn [] (reset! sent? true))]
        (.nativeKeyPressed listener event)
        (is @sent?))))
  (testing "ignores regular 1 key"
    (let [sent? (atom false)
          listener (sut/create-listener (atom {:config default-test-config}))
          event (make-num-event NativeKeyEvent/VC_1 NativeKeyEvent/KEY_LOCATION_STANDARD)]
      (with-redefs [sut/send-message (fn [] (reset! sent? true))]
        (.nativeKeyPressed listener event)
        (is (not @sent?)))))
  (testing "ignores other numpad keys"
    (let [sent? (atom false)
          listener (sut/create-listener (atom {:config default-test-config}))
          event (make-num-event NativeKeyEvent/VC_2 NativeKeyEvent/KEY_LOCATION_NUMPAD)]
      (with-redefs [sut/send-message (fn [] (reset! sent? true))]
        (.nativeKeyPressed listener event)
        (is (not @sent?)))))
  (testing "uses configurable key-code and key-location"
    (let [sent? (atom false)
          custom-config (assoc default-test-config
                               :key-code NativeKeyEvent/VC_F2
                               :key-location NativeKeyEvent/KEY_LOCATION_STANDARD)
          listener (sut/create-listener (atom {:config custom-config}))
          event (make-num-event NativeKeyEvent/VC_F2 NativeKeyEvent/KEY_LOCATION_STANDARD)]
      (with-redefs [sut/send-message (fn [] (reset! sent? true))]
        (.nativeKeyPressed listener event)
        (is @sent?))))
  (testing "ignores nativeKeyReleased and nativeKeyTyped"
    (let [sent? (atom false)
          listener (sut/create-listener (atom {:config default-test-config}))
          event (make-num-event NativeKeyEvent/VC_1 NativeKeyEvent/KEY_LOCATION_NUMPAD)]
      (with-redefs [sut/send-message (fn [] (reset! sent? true))]
        (.nativeKeyReleased listener event)
        (is (not @sent?))
        (.nativeKeyTyped listener event)
        (is (not @sent?))))))

(deftest lifecycle-test
  (testing "start! sets running? to true and stores listener"
    (let [orig @sut/app-state]
      (swap! sut/app-state assoc :running? false :listener nil)
      (with-redefs [sut/add-listener! (fn [_])]
        (sut/start!)
        (is (:running? @sut/app-state))
        (is (some? (:listener @sut/app-state))))
      (reset! sut/app-state orig)))
  (testing "stop! clears running? and listener"
    (let [mock-listener (Object.)
          orig @sut/app-state]
      (swap! sut/app-state assoc :running? true :listener mock-listener)
      (with-redefs [sut/remove-listener! (fn [_])]
        (sut/stop!)
        (is (not (:running? @sut/app-state)))
        (is (nil? (:listener @sut/app-state))))
      (reset! sut/app-state orig)))
  (testing "start! is idempotent when already running"
    (let [orig @sut/app-state]
      (swap! sut/app-state assoc :running? true :listener (Object.))
      (sut/start!)
      (is (:running? @sut/app-state))
      (reset! sut/app-state orig)))
  (testing "stop! is idempotent when not running"
    (let [orig @sut/app-state]
      (swap! sut/app-state assoc :running? false :listener nil)
      (sut/stop!)
      (is (not (:running? @sut/app-state)))
      (reset! sut/app-state orig))))
