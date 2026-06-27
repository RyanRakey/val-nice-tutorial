(ns val-nice-tutorial.settings
  (:import (javax.swing JDialog JPanel JLabel JTextField JButton
                        JRadioButton ButtonGroup JComboBox SwingUtilities)
           (java.awt GridBagLayout GridBagConstraints Insets FlowLayout Font Color)
           (java.awt.event ActionListener)
           (com.github.kwhat.jnativehook.keyboard NativeKeyEvent))
  (:require [val-nice-tutorial.config :as config]))

(def ^:private green (Color. 0 153 51))
(def ^:private red (Color. 153 0 0))

(def hotkey-options
  [{:label "Numpad 1" :key-code NativeKeyEvent/VC_1 :key-location NativeKeyEvent/KEY_LOCATION_NUMPAD}
   {:label "Numpad 2" :key-code NativeKeyEvent/VC_2 :key-location NativeKeyEvent/KEY_LOCATION_NUMPAD}
   {:label "Numpad 3" :key-code NativeKeyEvent/VC_3 :key-location NativeKeyEvent/KEY_LOCATION_NUMPAD}
   {:label "Numpad 4" :key-code NativeKeyEvent/VC_4 :key-location NativeKeyEvent/KEY_LOCATION_NUMPAD}
   {:label "Numpad 0" :key-code NativeKeyEvent/VC_0 :key-location NativeKeyEvent/KEY_LOCATION_NUMPAD}
   {:label "F2" :key-code NativeKeyEvent/VC_F2 :key-location NativeKeyEvent/KEY_LOCATION_STANDARD}
   {:label "F3" :key-code NativeKeyEvent/VC_F3 :key-location NativeKeyEvent/KEY_LOCATION_STANDARD}
   {:label "F4" :key-code NativeKeyEvent/VC_F4 :key-location NativeKeyEvent/KEY_LOCATION_STANDARD}
   {:label "F5" :key-code NativeKeyEvent/VC_F5 :key-location NativeKeyEvent/KEY_LOCATION_STANDARD}
   {:label "F6" :key-code NativeKeyEvent/VC_F6 :key-location NativeKeyEvent/KEY_LOCATION_STANDARD}
   {:label "Slash (/)" :key-code NativeKeyEvent/VC_SLASH :key-location NativeKeyEvent/KEY_LOCATION_STANDARD}])

(defn- find-hotkey-index
  [config]
  (first (keep-indexed (fn [i opt]
                         (when (and (= (:key-code opt) (:key-code config))
                                    (= (:key-location opt) (:key-location config)))
                           i))
                       hotkey-options)))

(defn- add-component
  [panel component & {:keys [gridx fill weightx anchor insets]
                      :or {gridx 1 anchor GridBagConstraints/WEST
                           insets (Insets. 4 0 4 4)}}]
  (let [c (GridBagConstraints.)]
    (set! (.gridx c) gridx)
    (set! (.anchor c) anchor)
    (set! (.insets c) insets)
    (when fill (set! (.fill c) fill))
    (when weightx (set! (.weightx c) weightx))
    (.add panel component c)))

(defn- build-hotkey-combo
  [config]
  (let [model (javax.swing.DefaultComboBoxModel.)
        combo (JComboBox. model)]
    (doseq [opt hotkey-options] (.addElement model (:label opt)))
    (when-let [idx (find-hotkey-index config)]
      (.setSelectedIndex combo idx))
    combo))

(defn- build-chat-mode-panel
  [config]
  (let [all-rd (JRadioButton. "All Chat (/all)" (= :all (:chat-mode config)))
        team-rd (JRadioButton. "Team Chat (/team)" (= :team (:chat-mode config)))
        _ (doto (ButtonGroup.) (.add all-rd) (.add team-rd))
        panel (JPanel. (FlowLayout. FlowLayout/LEFT 0 0))]
    (.add panel all-rd)
    (.add panel team-rd)
    {:chat-panel panel :all-rd all-rd}))

(defn- build-status-section
  [app-state]
  (let [running? (:running? @app-state)]
    {:status-label (doto (JLabel. (if running? "Running" "Stopped"))
                     (.setFont (.deriveFont (.getFont (JLabel.)) Font/BOLD (float 14)))
                     (.setForeground (if running? green red)))
     :start-btn (JButton. "Start")
     :stop-btn (JButton. "Stop")}))

(defn- build-save-action
  [dialog hotkey-combo msg-field all-rd app-state on-restart on-close]
  (reify ActionListener
    (actionPerformed [_ _]
      (let [selected-idx (.getSelectedIndex hotkey-combo)
            hotkey (nth hotkey-options selected-idx)
            new-config {:key-code (:key-code hotkey)
                        :key-location (:key-location hotkey)
                        :message (.getText msg-field)
                        :chat-mode (if (.isSelected all-rd) :all :team)}]
        (config/save-config new-config)
        (swap! app-state assoc :config new-config)
        (on-restart)
        (when on-close (on-close))
        (.dispose dialog)))))

(defn- bind-start-stop
  [start-btn stop-btn status-label on-start on-stop]
  (.addActionListener start-btn
                      (reify ActionListener
                        (actionPerformed [_ _]
                          (on-start)
                          (.setText status-label "Running")
                          (.setForeground status-label green))))
  (.addActionListener stop-btn
                      (reify ActionListener
                        (actionPerformed [_ _]
                          (on-stop)
                          (.setText status-label "Stopped")
                          (.setForeground status-label red)))))

(defn- show-dialog
  [dialog panel]
  (doto dialog
    (.add panel)
    (.pack)
    (.setResizable false)
    (.setLocationRelativeTo nil)
    (.setVisible true)))

(defn show-settings
  [app-state on-start on-stop on-restart on-close]
  (SwingUtilities/invokeLater
   (fn []
     (let [config (:config @app-state)
           dialog (JDialog.)
           panel (JPanel. (GridBagLayout.))

           {:keys [status-label start-btn stop-btn]} (build-status-section app-state)
           hotkey-combo (build-hotkey-combo config)
           {:keys [chat-panel all-rd]} (build-chat-mode-panel config)
           msg-field (JTextField. (:message config) 20)
           save-btn (JButton. "Save & Close")]

       (.setTitle dialog "Val Nice Tutorial Settings")
       (.setDefaultCloseOperation dialog JDialog/DISPOSE_ON_CLOSE)

       (add-component panel (JLabel. "Status:") :gridx 0 :insets (Insets. 4 4 4 8))
       (add-component panel status-label)
       (add-component panel (doto (JPanel. (FlowLayout. FlowLayout/LEFT 0 0))
                              (.add start-btn) (.add stop-btn)))
       (add-component panel (JLabel. "Trigger Key:") :gridx 0 :insets (Insets. 4 4 4 8))
       (add-component panel hotkey-combo :fill GridBagConstraints/HORIZONTAL :weightx 1.0)
       (add-component panel (JLabel. "Chat Mode:") :gridx 0 :insets (Insets. 4 4 4 8))
       (add-component panel chat-panel :fill GridBagConstraints/HORIZONTAL :weightx 1.0)
       (add-component panel (JLabel. "Message:") :gridx 0 :insets (Insets. 4 4 4 8))
       (add-component panel msg-field :fill GridBagConstraints/HORIZONTAL :weightx 1.0)
       (add-component panel save-btn :anchor GridBagConstraints/EAST :insets (Insets. 12 0 4 4))

       (bind-start-stop start-btn stop-btn status-label on-start on-stop)
       (.addActionListener save-btn
                           (build-save-action dialog hotkey-combo msg-field all-rd
                                              app-state on-restart on-close))
       (show-dialog dialog panel)))))
