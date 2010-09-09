(ns sexpbot.plugins.translate
  (:use [sexpbot respond utilities]
        [clojure.contrib.json :only [read-json]])
  (:require [clojure-http.resourcefully :as res])
  (:import org.apache.commons.lang.StringEscapeUtils))

(defn translate [lang1 lang2 text]
  (-> (res/get 
       "http://ajax.googleapis.com/ajax/services/language/translate"
       {} {"v" "1.0" "q" text "langpair" (str lang1 "|" lang2)})
      :body-seq first read-json))

(defplugin
  (:translate
   "Translates with google translate. Takes two language abbreviations (google's ones) and some text
   to translate, and returns it translated."
   ["trans" "translate"]
   [{:keys [irc bot channel args]}]
   (let [[lang-from lang-to & text] args
	 translation (translate lang-from lang-to (stringify text))]
     (if (:responseData translation)
       (send-message irc bot channel (-> translation 
                                         :responseData 
                                         :translatedText 
                                         StringEscapeUtils/unescapeHtml
                                         (.replaceAll "\n|\r" "")))
       (send-message irc bot channel "Languages not recognized.")))))