(ns llm-creating.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- env [k default]
  (or (System/getenv k) default))

(defn load-config
  "resources/config.edn を読み、環境変数で上書きする。"
  []
  (let [file (io/resource "config.edn")
        base (when file (-> file slurp edn/read-string))
        base-url (env "OLLAMA_BASE_URL" (get-in base [:ollama :base-url]))
        default-model (env "OLLAMA_DEFAULT_MODEL" (get-in base [:ollama :default-model]))]
    (cond-> (or base {})
      base-url (assoc-in [:ollama :base-url] (str/replace base-url #"/$" ""))
      default-model (assoc-in [:ollama :default-model] default-model))))
