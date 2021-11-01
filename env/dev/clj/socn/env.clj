(ns socn.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [socn.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[socn started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[socn has shut down successfully]=-"))
   :middleware wrap-dev})
