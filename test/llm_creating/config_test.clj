(ns llm-creating.config-test
  (:require [clojure.test :refer [deftest is testing]]
            [llm-creating.config :as config]))

(deftest load-config-test
  (testing "defaults from resources"
    (let [c (config/load-config)]
      (is (string? (get-in c [:ollama :base-url])))
      (is (string? (get-in c [:ollama :default-model]))))))
