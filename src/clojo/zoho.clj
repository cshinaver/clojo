(ns clojo.zoho
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [cheshire.core :refer :all]))

(defn- extract-auth-token [s]
  (last (re-find #"AUTHTOKEN=([^\n]*)" s)))

(defn generate-authentication-token
  [email_id password]
  (let [post-url "https://accounts.zoho.com/apiauthtoken/nb/create"
        scope "Zohopeople/peopleapi"]
    (extract-auth-token (:body (client/post
                                post-url
                                {:form-params {:SCOPE scope
                                               :EMAIL_ID email_id
                                               :PASSWORD password}})))))

(defn- start-timer-request [email_id auth_token]
  (let [url
        (format
         "https://people.zoho.com/people/api/timetracker/timer?authtoken=%s&user=%s&jobName=%s&workDate=%s&timer=start&billingStatus=Billable"
         auth_token
         email_id
         "Software Engineering"
         (l/format-local-time (l/local-now) :date))]
    (client/post url {:as :json})))


(defn start-policystat-timer
  [email_id auth_token]
  (let [errors (:errors (:response (:body (start-timer-request email_id auth_token))))]
    errors))

(defn get-running-timers
  [auth_token]
  (let [url (format
             "http://people.zoho.com/people/api/timetracker/getcurrentlyrunningtimer?authtoken=%s"
             auth_token)]
    (client/get url {:as :json})))

(defn- stop-timer-request [auth_token]
  (let [url
        (format
         "https://people.zoho.com/people/api/timetracker/timer?authtoken=%s&timeLogId=%s&timer=stop"
         auth_token
         (:timelogId (:result (:response (:body (get-running-timers auth_token))))))]
    (client/post url {:throw-exceptions false})))

(defn stop-policystat-timer
  ; Returns 0 if timer stopped
  ; Returns 1 if no timer found
  ; Returns -1 if unknown status code
  [email_id auth_token]
  (let [status (:status (stop-timer-request auth_token))]
    (cond
      (= status 400) 1 ; No timer found
      (= status 200) 0 ; timer stopped
      :else status))) ; Unknown status code error


(defn- get-time-logs
  [email_id from_date to_date auth_token]
  (let [content
        (let [time-log-url
              (format
               "http://people.zoho.com/people/api/timetracker/gettimelogs?authtoken=%s&user=%s&jobId=%s&fromDate=%s&toDate=%s&billingStatus=%s"
               auth_token ; Auth token
               email_id; User
               "all" ; jobId
               from_date ; fromDate 2015-10-11
               to_date ; toDate 2015-10-12
               "all"; billingStatus
)]
          (client/get time-log-url {:as :json}))]
    (-> content; decode response
        :body
        :response
        :result)))

(defn get-timesheets
  [email_id auth_token]
  (let [content
        (let [timesheet-url
              (format
               "http://people.zoho.com/people/api/timetracker/gettimesheet?authtoken=%s&user=%s&approvalStatus=%s"
               auth_token ; Auth token
               email_id; User
               "all" ; approvalStatus
)]
          (client/get timesheet-url {:as :json}))]
    (-> content; decode response
        :body
        :response
        :result)))

(defn- is-within-date
  [check-date
   begin-date
   end-date]
  ((fn [ls]
     (t/within? (t/interval (nth ls 1) (nth ls 2)) (nth ls 0))) ; Check if within range
   (map (fn [x] (f/parse (f/formatter "yyyy-MM-dd") x)) [check-date begin-date end-date])))

(defn- is-log-in-timesheet
  [timelog timesheet]
  (let [work-date (:workDate timelog)
        begin-date (:fromDate timesheet)
        end-date (:toDate timesheet)]
    (is-within-date work-date begin-date end-date)))

(defn- get-unsubmitted-hours
  [from_date to_date email_id auth_token]
  (let
   [hour-logs (get-time-logs email_id from_date to_date auth_token)
    timesheets (get-timesheets email_id auth_token)]
    (filter (fn [log] (not-any? #(is-log-in-timesheet log %) timesheets)) hour-logs)))
