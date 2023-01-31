(ns sample.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [sample.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[sample started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[sample has shut down successfully]=-"))
   :middleware wrap-dev})
