(ns clojo.zoho
    (:require [clj-http.client :as client]
              [clojure.data.json :as json]
              [cheshire.core :refer :all]
              )
  )

(defn generate-authentication-token
  [email_id password]
  (defn extract-auth-token [s]
    (last (re-find #"AUTHTOKEN=([^\n]*)" s))
    )
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
  [auth_token]
  (let [content (let [
                      time-log-url (format
                                     "http://people.zoho.com/people/api/timetracker/gettimelogs?authtoken=%s&fromDate=%s&toDate=%s&billingStatus=%s"
                                     auth_token ; Auth token
                                     "2015-10-01" ; fromDate
                                     "2015-10-12" ; toDate
                                     "all"; billingStatus
                                     )
                      ]
                  (client/get time-log-url {:as :json}))]
    (-> content; decode response
        :body
        :response
        )
    ))
