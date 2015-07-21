
(ns pva-parrot.app
  (:require [taoensso.encore :as enc    :refer (tracef debugf infof warnf errorf)]
            [pva-parrot.plot :as plot]
            [pva-parrot.websockets :as websockets]
            [reagent.core :as reagent :refer [atom]]))


(def app-state (atom {:page :welcome
                      :project {:name "My Project"}
                      :import-file nil
                      :parsed-data nil
                      :eigen-values nil
                      :eigen-vectors nil
                      }))


(defn swap-page [target]
  (swap! app-state assoc :page target))

(defn store-text-file!
  "Reads a file as text and swaps the result into the app-state for the given key."
  [file key]
  (let [reader (js/FileReader.)]
    (set! (.-onload reader)
      (fn [event]
        (let [file-content (aget event "target" "result")]
          (swap! app-state assoc key file-content))))
    (.readAsText reader file)))

(defn import-file!
  [event]
  (let [file (aget event "target" "files" 0)]
    (store-text-file! file :import-file)))

(defn styled-button [& opts]
  (let [{:keys [style size click-handler contents]} opts
        classes (clojure.string/join " " [(when style (str "btn-" style))
                                          (when size  (str "btn-" size))])]
    [:button.btn {:class classes :on-click click-handler} contents]))

(defn pages-button [target text & opts]
  [styled-button
    :contents text
    :style "primary"
    :size "lg"
    :click-handler #(swap-page target)])

(defn back-button [target]
  [styled-button
    :size "sm"
    :contents "back"
    :click-handler #(swap-page target)])

(defn file-input []
  [:input.btn {:name "import-file" :type "file" :accept "text/csv"
               :on-change import-file!}])

(defn submit-file [this]
  (let [file-body (:import-file @app-state)
        event-data {:file-body file-body}]
    (.preventDefault this)
    (websockets/chsk-send! [:data-import/csv-sent event-data])))

(defn submit-file-component [text]
  [:form.form-inline.well

   [:div.form-group
    [:label {:for "import-file"} text]
    [file-input]]

   [styled-button :style "primary"
    :contents "Import it"
    :click-handler submit-file]])

(defn table-header [headings]
  [:tr (map (fn [cell] [:th.dt-center cell]) headings)])

(defn table-row [row]
  [:tr (map (fn [cell] [:td.dt-right cell]) row)])

(defn table [{:keys [body headings]}]
  (if body
    [:table.table.display.compact
     (when headings
       [:thead
        [table-header headings]])
     [:tbody
      (map table-row body)
      ]]
    [:div]))

(defn refresh-table [comp]
  (.DataTable (js/$ (reagent/dom-node comp))))

(defn table-component [data]
  (reagent/create-class {:reagent-render table
                         :component-did-mount refresh-table
                         :component-did-update refresh-table}))

(defn accordion-section [{:keys [key default title]} & contents]
  (let [id (str "collapse-" key)
        heading-id (str "heading-" key)]
    [:div.panel.panel-default
     [:div.panel-heading {:role "tab" :id heading-id}
      [:h1.panel-title
       [:a {:data-toggle "collapse"
            :href (str "#" id) :aria-extended true
            :aria-contols id}
        title]]]
     [:div.panel-collapse.collapse.in {:role "tabpanel" :id id
                                       :aria-labelledby heading-id}
      [:div.panel-body
       contents]
      ]]))

(defn accordion-component [sections]
  [:div.panel-group {:role "tablist"}
   (for [section sections]
     (let [{:keys [key title content]} section]
       [accordion-section {:key key :title title} content]))])

(defn workbook-section-data []
  [:div
   [submit-file-component "Import CSV Dataset"]
   [:span (str "Samples: " (@app-state :num-samples))]
   [:span (str ", Variables: " (@app-state :num-variables))]
   [:h2 "Variables Summary"]
   [:h3 "Raw Data"]
   [table-component (@app-state :summary-variables)]
   [:h3 "Normalized Data"]
   [table-component (@app-state :summary-normalized)]
   [:h2 "Dataset:"]
   [table-component (@app-state :parsed-data)]
   ])

(defn workbook-section-components []
  [:div
   [:form.form.well
    [:p "Expected number of components"]
    [:div.form-group
     [:label {:for "components-min"} "min."]
     [:input {:name "components-min" :type "number" :min 2 :value 2}]]
    [:div.form-group
     [:label {:for "components-max"} "max."]
     [:input {:name "components-max" :type "number" :min 3 :value 5}]]]
   [:h3 "Eigenvalues"]
   [table-component (@app-state :eigen-values)]
   [:h3 "Eigenvectors"]
   [table-component (@app-state :eigen-vectors)]
   ])

(defn workbook-section-proportions []
  [:div])

(defn pva-workbook [project]
  [:div
   [:div.col-lg-12
    [back-button :welcome]]
   [:div.row
    [:div.col-lg-12
     [:h1 (str "Project: " (:name @project))]
     [accordion-component
      [{:key "analytical-data"
        :title "1. Analytical Data"
        :content [workbook-section-data]}
       {:key "sign-components"
        :title "2. Significant Components"
        :content [workbook-section-components]}
       {:key "comp-and-props"
        :title "3. Compositions and Proportions"
        :content [workbook-section-proportions]}
       ]]]]])
(defn welcome-page []
  [:div#welcome-page
   [:div.jumbotron
    [:img.img-responsive {:src "img/parrot.png"}]
    [:h1 "PVA Parrot"]
    [:h2 "An application for polytopic vector analysis"]
    [:hr]
    [pages-button :pva-workbook "Go to Workbook"]]])

(defn page-component []
  (let [pages {:welcome [welcome-page]
               :pva-workbook [pva-workbook
                              (reagent/cursor app-state [:project])]}]
    ((:page @app-state) pages)))


(defmethod websockets/handle-event :data-import/file-returned
  [[_ data]]
  (debugf "Imported raw data and PCA data received.")
  (swap! app-state assoc :parsed-data (:parsed-data data))
  (swap! app-state assoc :summary-variables (:summary-variables data))
  (swap! app-state assoc :summary-normalized (:summary-normalized data))
  (swap! app-state assoc :num-variables (:num-variables data))
  (swap! app-state assoc :num-samples (:num-samples data))
  (swap! app-state assoc :eigen-values (:eigen-values data))
  (swap! app-state assoc :eigen-vectors (:eigen-vectors data)))


(defn init []
  (reagent.core/render-component [page-component]
    (js/document.getElementById "container")))
