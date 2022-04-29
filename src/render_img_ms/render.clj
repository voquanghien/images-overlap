(ns render-img-ms.render
  (:require [fivetonine.collage.core :refer :all]
            [clojure.data.codec.base64 :as base64]
            [clojure.data.json :as json])
  (:import (java.io ByteArrayOutputStream)
           (java.net URL)
           (javax.imageio ImageIO)
           (java.awt Color)
           (java.awt.image BufferedImage)
           (clojure.lang PersistentVector)))

(defn- headers
  "Generic CORS headers"
  []
  {"Access-Control-Allow-Origin"  "*"
   "Access-Control-Allow-Headers" "*"
   "Access-Control-Allow-Methods" "GET"
   "Content-Type" "image/png"})

(defn- base64-string->data
  "base64 string -> json string -> clojure data"
  [^String base64str]
  (let [json-str (String. (byte-array (base64/decode (.getBytes base64str))))
        data (json/read-str json-str :key-fn keyword)]
    data))

(defn- string?->int
  "Check if input is string then convert to int else return int"
  [input]
  (Math/round (double (if (string? input) (read-string input) input))))

(defn- create-empty-buffered-image
  "Creates a transparent empty BufferedImage with width and height input."
  [^Integer width ^Integer height]
   (let [buf-img (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
         graphics (.createGraphics buf-img)]
     (doto graphics
       (.setBackground (Color. 0 0 0 0))
       (.clearRect 0 0 width height)
       (.dispose))
     buf-img))

(defn- get-layer-measure-data
  "Return sequence of  width and height of all layers with width equal to x + w, height equal to y + h"
  [^PersistentVector data]
  (for [layer data
        :let [x (string?->int (layer :x))
              y (string?->int (layer :y))
              w (string?->int (layer :width))
              h (string?->int (layer :height))]]
    {:width (+ x w)
     :height (+ y h)}))

(defn- create-blank-background
  "Create blank background width max width and max height derived from data"
  [^PersistentVector data]
  (let [measure-data (get-layer-measure-data data)
        width ((apply max-key :width measure-data) :width)
        height ((apply max-key :height measure-data) :height)
        rs (with-image (create-empty-buffered-image width height))]
    rs)
  )

(defn- get-layer-data
  "get layer from url than resize based on input w h"
  [^String url ^Integer w ^Integer h]
  (let [rs (with-image (URL. url)
                       (resize :width w :height h))]
    rs)
  )

(defn- create-layers-data
  "create layers used to paste into blank background from data input"
  ;; format: [layer1 x1 y1 layer2 x2 y2 ...]
  [^PersistentVector data]
  (flatten (for [layer data
                 :let [url (layer :url)
                       x (string?->int (layer :x))
                       y (string?->int (layer :y))
                       w (string?->int (layer :width))
                       h (string?->int (layer :height))
                       img (get-layer-data url w h)]]
             [img x y])))

(defn- paste-layers-into-background
  "return new image after pasting layers to blank background following format of collage/paste"
  ;; first input will be blank buffered image
  ;; second input will be an array of layers' data
  [^PersistentVector data]
  (let [bg (create-blank-background data)
        layers (create-layers-data data)]
    (paste bg layers)))

(defn- buffered-image->png-image-byte-array
  "convert buffered image to png image byte array used to return png in response"
  [^BufferedImage buffered-img]
  (let [os (ByteArrayOutputStream.)
        ;; more clearly convention about the mutation
        _ (ImageIO/write buffered-img "png" os)
        ba (.toByteArray os)]
    ba)
  )

(defn render-image
  "return final (merged) image in response based on base64 string input"
  [{:keys [parameters]}]
  (let [base64-str (get-in parameters [:path :base64])
        data (base64-string->data base64-str)
        merged-img (paste-layers-into-background data)
        ba (buffered-image->png-image-byte-array merged-img)
        ]
    {:status 200
     :headers (headers)
     :body ba})
  )

(def render-image-memoized (memoize render-image))
