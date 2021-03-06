(ns slack-signup.web
  (:require [slack-signup.core :as slack]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [environ.core :refer [env]]))

(use 'ring.middleware.resource)

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (slurp (io/resource "signup.html"))})

(defroutes app
  (GET "/" []
       (splash))
  (POST "/signup" req
    (slack/request-invite-handler req))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(def handler
  (-> (site #'app)
      (wrap-json-response)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-resource "public")
      (wrap-content-type)))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty
      handler
      {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
