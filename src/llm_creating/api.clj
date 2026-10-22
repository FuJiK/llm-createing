(ns llm-creating.api
  "アプリから呼び出すための薄いファサード。"
  (:require [llm-creating.config :as config]
            [llm-creating.docker :as docker]
            [llm-creating.ollama :as ollama]))

(defn config [] (config/load-config))

(defn start-stack! [] (docker/up!))

(defn stop-stack! [] (docker/down!))

(defn ensure-model!
  "モデルが無ければ pull する。"
  [model]
  (let [opts (:ollama (config))]
    (when-not (some #(= model (:name %)) (ollama/list-models opts))
      (ollama/pull-model! opts model))))

(defn ask
  "1 回の質問に答える。"
  [prompt & {:keys [model system]}]
  (let [c (config)
        m (or model (get-in c [:ollama :default-model]))
        opts (:ollama c)]
    (ensure-model! m)
    (ollama/generate opts {:model m :prompt prompt :system system})))
