(ns clojo.core
  (:gen-class)
  (:require
   [clj-http.client :as client]
   [clojo.zoho :as zoho]
   [clojure.java.io :as io]))

(def user-preference-file (str (System/getProperty "user.home") "/" ".clojo"))

(defn- generate-new-auth-token [email password]
  (println "Generating new authentication token")
  (let
   [auth_token (zoho/generate-authentication-token email password)]
    (if-not (nil? auth_token)
     (do  (println (str "Auth token " auth_token " generated"))
           auth_token)
      (println "Failure in generating auth token"))))

(defn- get-auth-token-from-file []
  (slurp user-preference-file))

(defn- get-auth-from-user []
  (do
    (println "Please enter zoho email and password")
    (let [email (read-line) password (read-line)]
      (let [auth_token (generate-new-auth-token email password)]
        (println "Authentication token not saved")
        (println (str "Saving auth token to " user-preference-file))
        (spit user-preference-file email)
        (spit user-preference-file "\n" :append true)
        (spit user-preference-file auth_token :append true)
        {:email_id email :auth_token auth_token}))))

(defn- get-auth-from-file []
  ((fn [info] {:email_id (first info) :auth_token (second info)})
     (clojure.string/split-lines (slurp user-preference-file))))
                           

(defn get-auth-info []
  (if (.exists (io/as-file user-preference-file))
    (let [creds (get-auth-from-file)]
      (if (nil? (:auth_token creds))
        (get-auth-from-user)
        creds))
    (get-auth-from-user)))

(defn- non-trivial-parse-args [arg]
  (let [auth_info (get-auth-info)]
    (let [email_id (:email_id auth_info) auth_token (:auth_token auth_info)]
      (cond
        (= arg "--status")
        ((fn [content]
           (let [running-timers (-> content :body :response :result)]
             (if (nil? running-timers)
               (println "No running timers.")
               (let [timer-info (clojo.zoho/get-timer-info auth_token)]
                 (println
                  (format
                   "There has been a running timer for %d hours, %d minutes and %d seconds"
                   (:hours timer-info)
                   (:minutes timer-info)
                   (:seconds timer-info)))
                 (println
                  (format
                   "Total money made: $%.2f"
                   (:current-sum timer-info)))))))
         (zoho/get-running-timers auth_token))
        (= arg "--start-timer")
        (let [timer-started-error (zoho/start-policystat-timer email_id auth_token)]
          (if (nil? timer-started-error)
            (println "Timer started successfully")
            (println (format "Error: %s" (:message timer-started-error)))))
        (= arg "--stop-timer")
        (let [email_id (:email_id auth_info) auth_token (:auth_token auth_info)]
          (let
           [return_status (zoho/stop-policystat-timer email_id auth_token)]
            (cond
              (zero? return_status)
              (println "Timer stopped successfully.")
              (= return_status 1)
              (println "No timer found.")
              :else return_status)))))))

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
