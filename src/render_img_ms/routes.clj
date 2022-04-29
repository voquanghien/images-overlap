(ns render-img-ms.routes
  (:require [render-img-ms.render :as render]))

(def render-routes
  ["/render"
   {:swagger {:tags ["render"]}}
   ["/:base64"
    {:parameters {:path {:base64 string?}}
     :get render/render-image}]
   ["/memoize"
    ["/:base64"
     {:parameters {:path {:base64 string?}}
      :get render/render-image-memoized}]]])
