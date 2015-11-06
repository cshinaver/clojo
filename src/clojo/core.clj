(ns clojo.core
  (:gen-class)
  (:require
   [clj-http.client :as client]
   [clojo.zoho :as zoho]
   [clojure.java.io :as io]))

(def user-preference-file (str (System/getProperty "user.home") "/" ".clojo"))

(defn- generate-new-auth-token []
  (println "Authentication token not saved")
  (println "Enter Zoho email address and password")
  (let
      ;[email (read-line) password (read-line)]
   [email "test" password "pass"]
    (println "Generating new authentication token")
    (let
        ;[auth_token (zoho/generate-authentication-token email password)]
     [auth_token (str email "wut" password)]
      (println (str "Auth token " auth_token " generated"))
      (println (str "Saving auth token to " user-preference-file))
      (spit user-preference-file auth_token)
      auth_token)))

(defn- get-auth-token-from-file []
  (slurp user-preference-file))

(defn- get-auth-token []
  (if (not (.exists (io/as-file user-preference-file)))
    (generate-new-auth-token)
    (if (nil? (resolve 'auth_token))
      (get-auth-token-from-file)
      "lol")))

(defn- non-trivial-parse-args [arg]
  (cond
    (= arg "--status") (zoho/get-running-timers (get-auth-token))))

(defn parse-args [args]
  (let [n (count args)]
    (cond
      (= n 0) (println (str
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
