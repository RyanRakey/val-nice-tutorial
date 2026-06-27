(ns val-nice-tutorial.tray
  (:import (java.awt SystemTray TrayIcon PopupMenu MenuItem)
           (java.awt.image BufferedImage)
           (java.awt Color RenderingHints Font)
           (javax.swing SwingUtilities)))

(defn create-icon
  []
  (let [img (BufferedImage. 16 16 BufferedImage/TYPE_INT_ARGB)
        g (.createGraphics img)]
    (doto g
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setColor (Color. 51 153 255))
      (.fillOval 0 0 15 15)
      (.setColor Color/WHITE)
      (.setFont (Font. "SansSerif" Font/BOLD 10))
      (.drawString "V" 4 12))
    (.dispose g)
    img))

(defn update-tray-menu
  [popup start-item stop-item settings-item quit-item running?]
  (.removeAll popup)
  (.add popup (doto start-item (.setEnabled (not running?))))
  (.add popup (doto stop-item (.setEnabled running?)))
  (.add popup settings-item)
  (.addSeparator popup)
  (.add popup quit-item))

(defn setup-tray!
  [app-state on-start on-stop on-settings]
  (when (SystemTray/isSupported)
    (SwingUtilities/invokeLater
     (fn []
       (let [tray (SystemTray/getSystemTray)
             icon (TrayIcon. (create-icon) "Val Nice Tutorial")
             popup (PopupMenu.)
             start-item (MenuItem. "Start")
             stop-item (MenuItem. "Stop")
             settings-item (MenuItem. "Settings")
             quit-item (MenuItem. "Quit")
             update-fn (fn []
                         (update-tray-menu popup start-item stop-item
                                           settings-item quit-item
                                           (:running? @app-state)))]
         (.addActionListener start-item
                             (reify java.awt.event.ActionListener
                               (actionPerformed [_ _] (on-start) (SwingUtilities/invokeLater update-fn))))
         (.addActionListener stop-item
                             (reify java.awt.event.ActionListener
                               (actionPerformed [_ _] (on-stop) (SwingUtilities/invokeLater update-fn))))
         (.addActionListener settings-item
                             (reify java.awt.event.ActionListener
                               (actionPerformed [_ _] (on-settings update-fn))))
         (.addActionListener quit-item
                             (reify java.awt.event.ActionListener
                               (actionPerformed [_ _] (on-stop) (System/exit 0))))
         (update-fn)
         (.setPopupMenu icon popup)
         (.add tray icon)
         icon)))))
