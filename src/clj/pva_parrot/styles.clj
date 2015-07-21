
(ns pva-parrot.styles
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule]]))

(defstyles base
  [:* {:box-sizing "border-box"}]
  [:body
   {:padding "10px"
    :font-family "Helvetica Neue"
    :font-size   "16px"
    :line-height 1.5}
   [:#welcome-page
    [:img
     {:max-width "400px"
      :float "left"}]]
   [:.panel
    [:.panel-title
     {:text-align "center"
      :font-size "25px"}]]])

(defstyles grid
  [:.top-buffer { :margin-top "20px"}])
