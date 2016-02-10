(ns lunchselector.core-test
  (:require [clojure.test :refer :all]
            [lunchselector.core :as core]
            [lunchselector.utils :as utils]))

;; Initialize the app configuration
(utils/initialize-app-configuration)

(deftest restaurants-test
  (is (= (:status (core/restaurants {:params {"keyword" "bad"}}))
         200)))
