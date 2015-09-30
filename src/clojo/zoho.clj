(ns clojo.zoho
    (:require [clj-http.client :as client])
  )

(defn generate-authentication-token
  [email_id password]
  (defn extract-auth-token [s]
    (re-find #"AUTHTOKEN=[^\n]*" s)
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
