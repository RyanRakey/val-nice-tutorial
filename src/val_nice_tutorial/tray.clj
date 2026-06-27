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

(defn- menu-item ^MenuItem [label] (MenuItem. label))

(defn- action-listener ^java.awt.event.ActionListener [f]
  (reify java.awt.event.ActionListener
    (actionPerformed [_ _] (f))))

(defn- make-update-fn
  [popup {:keys [start-item stop-item settings-item quit-item]} app-state]
  (fn []
    (update-tray-menu popup start-item stop-item settings-item quit-item
                      (:running? @app-state))))

(defn- bind-start-stop
  [item on-fn update-fn]
  (.addActionListener item (action-listener #(do (on-fn) (SwingUtilities/invokeLater update-fn)))))

(defn setup-tray!
  [app-state on-start on-stop on-settings]
  (when (SystemTray/isSupported)
    (SwingUtilities/invokeLater
     (fn []
       (let [tray (SystemTray/getSystemTray)
             icon (TrayIcon. (create-icon) "Val Nice Tutorial")
             popup (PopupMenu.)
             items {:start-item (menu-item "Start")
                    :stop-item (menu-item "Stop")
                    :settings-item (menu-item "Settings")
                    :quit-item (menu-item "Quit")}
             update-fn (make-update-fn popup items app-state)]
         (bind-start-stop (:start-item items) on-start update-fn)
         (bind-start-stop (:stop-item items) on-stop update-fn)
         (.addActionListener (:settings-item items)
                             (action-listener #(on-settings update-fn)))
         (.addActionListener (:quit-item items)
                             (action-listener #(do (on-stop) (System/exit 0))))
         (update-fn)
         (.setPopupMenu icon popup)
         (.add tray icon)
         icon)))))
