(defproject render-img-ms "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.0"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [metosin/reitit "0.5.17"]
                 [fivetonine/collage "0.3.0"]
                 [org.clojure/data.codec "0.1.1"]
                 [org.clojure/data.json "2.4.0"]]
  :repl-options {:init-ns render-img-ms.core}
  :aot [render-img-ms.core]
  :main render-img-ms.core
  :jvm-opts ["-Xms1g" "-Xmx4g"])
