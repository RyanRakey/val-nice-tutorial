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

(defn- add-label
  [panel text]
  (let [c (GridBagConstraints.)]
    (set! (.gridx c) 0)
    (set! (.anchor c) GridBagConstraints/WEST)
    (set! (.insets c) (Insets. 4 4 4 8))
    (.add panel (JLabel. text) c)))

(defn- add-field
  [panel component]
  (let [c (GridBagConstraints.)]
    (set! (.gridx c) 1)
    (set! (.fill c) GridBagConstraints/HORIZONTAL)
    (set! (.weightx c) 1.0)
    (set! (.insets c) (Insets. 4 0 4 4))
    (.add panel component c)))

(defn- add-full-width
  [panel component anchor insets]
  (let [c (GridBagConstraints.)]
    (set! (.gridx c) 1)
    (set! (.anchor c) anchor)
    (set! (.insets c) insets)
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

(defn show-settings
  [app-state on-start on-stop on-restart on-close]
  (SwingUtilities/invokeLater
   (fn []
     (let [config (:config @app-state)
           dialog (doto (JDialog.) (.setTitle "Val Nice Tutorial Settings"))
           panel (JPanel. (GridBagLayout.))

           {:keys [status-label start-btn stop-btn]} (build-status-section app-state)
           hotkey-combo (build-hotkey-combo config)
           {:keys [chat-panel all-rd]} (build-chat-mode-panel config)
           msg-field (JTextField. (:message config) 20)
           save-btn (JButton. "Save & Close")]

       (add-label panel "Status:")
       (add-full-width panel status-label GridBagConstraints/WEST (Insets. 4 0 4 4))
       (add-full-width panel (doto (JPanel. (FlowLayout. FlowLayout/LEFT 0 0))
                               (.add start-btn) (.add stop-btn))
                       GridBagConstraints/WEST (Insets. 4 0 4 4))
       (add-label panel "Trigger Key:")
       (add-field panel hotkey-combo)
       (add-label panel "Chat Mode:")
       (add-field panel chat-panel)
       (add-label panel "Message:")
       (add-field panel msg-field)
       (add-full-width panel save-btn GridBagConstraints/EAST (Insets. 12 0 4 4))

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
                               (.setForeground status-label red))))
       (.addActionListener save-btn
                           (build-save-action dialog hotkey-combo msg-field all-rd
                                              app-state on-restart on-close))
       (.setDefaultCloseOperation dialog JDialog/DISPOSE_ON_CLOSE)
       (.add dialog panel)
       (.pack dialog)
       (.setResizable dialog false)
       (.setLocationRelativeTo dialog nil)
       (.setVisible dialog true)
       dialog))))
