(ns sample.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[sample started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[sample has shut down successfully]=-"))
   :middleware identity})
