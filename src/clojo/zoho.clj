(ns clojo.zoho
    (:require [clj-http.client :as client]
              [clojure.data.json :as json]
              [clj-time.core :as t]
              [clj-time.format :as f]
              [cheshire.core :refer :all]
              ))

(defn generate-authentication-token
  [email_id password]
  (defn extract-auth-token [s]
    (last (re-find #"AUTHTOKEN=([^\n]*)" s)))
  (let [
        post-url "https://accounts.zoho.com/apiauthtoken/nb/create"
        scope "Zohopeople/peopleapi"
        ]
    (extract-auth-token (:body (client/post
      post-url
      {:form-params {
                    :SCOPE scope
                    :EMAIL_ID email_id
                    :PASSWORD password}})))))

(defn get-time-logs
  [email_id auth_token]
  (let [content (let [
                      time-log-url (format
                                     "http://people.zoho.com/people/api/timetracker/gettimelogs?authtoken=%s&user=%s&jobId=%s&fromDate=%s&toDate=%s&billingStatus=%s"
                                     auth_token ; Auth token
                                     email_id; User
                                     "all" ; jobId
                                     "2015-10-11" ; fromDate
                                     "2015-10-12" ; toDate
                                     "all"; billingStatus
                                     )
                      ]
                  (client/get time-log-url {:as :json}))]
    (-> content; decode response
        :body
        :response
        :result)))

(defn get-timesheets
  [email_id auth_token]
  (let [content (let [
                      timesheet-url (format
                                     "http://people.zoho.com/people/api/timetracker/gettimesheet?authtoken=%s&user=%s&approvalStatus=%s"
                                     auth_token ; Auth token
                                     email_id; User
                                     "all" ; approvalStatus
                                     )
                      ]
                  (client/get timesheet-url {:as :json}))]
    (-> content; decode response
        :body
        :response
        :result)))

