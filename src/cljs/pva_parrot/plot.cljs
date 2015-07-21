
(ns pva-parrot.plot
  (:require [reagent.core :as reagent]))

(defn- plot
  ([comp]
   (let [data [{:label "foo"
                :points {:show true}
                :color "#E72510"
                :data (-> comp reagent/props :data)}]
         plot-options {:grid {:hoverable true
                              :clickable true}}]
     (.plot js/$ (reagent/dom-node comp)
       (clj->js data)
       (clj->js plot-options)))))

(defn plot-component []
  (reagent/create-class
    {:component-did-mount plot
     :component-did-update plot
     :display-name "plot-component"
     :reagent-render (fn [] [:div.plot-container
                            {:style {:width "100%"
                                     :height "500px"}}])}))
