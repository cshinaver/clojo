(ns clojo.core
  (:gen-class)
  (:require
    [clj-http.client :as client]
    [clojo.zoho :as zoho])
  )

(defn parse-args [& args]
(defn -main
  (let [n (count args)]
    (cond
      (= n 1) (println (str
                         "usage: java -jar clojo.jar "
                         "[--status] "
                         "[--start-timer] "
                         "[--stop-timer] "))
      :else n
      ))
  )

  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
