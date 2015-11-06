(ns clojo.core
  (:gen-class)
  (:require
   [clj-http.client :as client]
   [clojo.zoho :as zoho]
   [clojure.java.io :as io]))

(def user-preference-file (str (System/getProperty "user.home") "/" ".clojo"))

(defn- generate-new-auth-token []
  (println "Enter Zoho email address and password")
  (let
   [email (read-line) password (read-line)]
    (println "Generating new authentication token")
    (let
     [auth_token (zoho/generate-authentication-token email password)]
      (if-not (nil? auth_token)
        (do  (println (str "Auth token " auth_token " generated"))
             auth_token)
        (println "Failure in generating auth token")))))

(defn- get-auth-token-from-file []
  (slurp user-preference-file))

(defn- get-auth-token []
  (if-not (.exists (io/as-file user-preference-file))
    (let [auth_token (generate-new-auth-token)]
      (println "Authentication token not saved")
      (println (str "Saving auth token to " user-preference-file))
      (spit user-preference-file auth_token)
      auth_token)
    (slurp user-preference-file)))

(defn- non-trivial-parse-args [arg]
  (cond
    (= arg "--status")
    ((fn [content]
       (let [running-timers (-> content :body :response :status)]
         (if (= running-timers 0)
           (println "No running timers.")
           (println "There is a running timer"))))
     (zoho/get-running-timers (get-auth-token)))))

(defn parse-args [args]
  (let [n (count args)]
    (cond
      (zero? n) (println (str
                          "usage: java -jar clojo.jar "
                          "[--status] "
                          "[--start-timer] "
                          "[--stop-timer] "))
      (= n 1) (non-trivial-parse-args (first args))
      :else n)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (parse-args args))

(get-auth-token)
