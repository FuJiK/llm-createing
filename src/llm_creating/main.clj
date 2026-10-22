(ns llm-creating.main
  (:require [clojure.string :as str]
            [llm-creating.config :as config]
            [llm-creating.docker :as docker]
            [llm-creating.ollama :as ollama])
  (:gen-class))

(defn- usage []
  (println "Usage: clj -M:run <command> [args...]")
  (println)
  (println "Commands:")
  (println "  up              docker compose で Ollama を起動")
  (println "  down            Ollama を停止")
  (println "  ps              コンテナ状態")
  (println "  logs [N]        ログ（デフォルト 100 行）")
  (println "  health          API が生きているか")
  (println "  models          インストール済みモデル一覧")
  (println "  pull [MODEL]    モデルを pull（未指定時は config の default）")
  (println "  generate PROMPT テキスト生成")
  (println "  chat PROMPT     チャット API で 1 ターン"))

(defn- cfg []
  (config/load-config))

(defn- ollama-opts [c]
  (:ollama c))

(defn- exit [code]
  (System/exit code))

(defn -main
  [& args]
  (let [cmd (first args)
        rest-args (rest args)]
    (case cmd
      "up"
      (let [{:keys [exit stderr stdout]} (docker/up!)]
        (when (not (str/blank? stdout)) (println stdout))
        (when (not (str/blank? stderr)) (binding [*out* *err*] (println stderr)))
        (exit exit))

      "down"
      (let [{:keys [exit stderr stdout]} (docker/down!)]
        (when (not (str/blank? stdout)) (println stdout))
        (when (not (str/blank? stderr)) (binding [*out* *err*] (println stderr)))
        (exit exit))

      "ps"
      (let [{:keys [stdout exit]} (docker/ps)]
        (println stdout)
        (exit exit))

      "logs"
      (let [tail (or (first rest-args) "100")
            {:keys [stdout exit]} (docker/logs :tail tail)]
        (println stdout)
        (exit exit))

      "health"
      (if (ollama/health? (ollama-opts (cfg)))
        (do (println "ok") (exit 0))
        (do (println "unreachable") (exit 1)))

      "models"
      (if-let [models (ollama/list-models (ollama-opts (cfg)))]
        (doseq [m models]
          (println (:name m)))
        (exit 1))

      "pull"
      (let [c (cfg)
            model (or (first rest-args) (get-in c [:ollama :default-model]))
            _ (println (str "Pulling " model " ..."))
            result (ollama/pull-model! (ollama-opts c) model)]
        (println (or result "done"))
        (exit 0))

      "generate"
      (let [prompt (str/join " " rest-args)]
        (when (str/blank? prompt)
          (usage)
          (exit 1))
        (let [c (cfg)
              model (get-in c [:ollama :default-model])
              text (ollama/generate (ollama-opts c) {:model model :prompt prompt})]
          (if text
            (do (println text) (exit 0))
            (do (println "generate failed") (exit 1)))))

      "chat"
      (let [prompt (str/join " " rest-args)]
        (when (str/blank? prompt)
          (usage)
          (exit 1))
        (let [c (cfg)
              model (get-in c [:ollama :default-model])
              text (ollama/chat (ollama-opts c)
                                {:model model
                                 :messages [{:role "user" :content prompt}]})]
          (if text
            (do (println text) (exit 0))
            (do (println "chat failed") (exit 1)))))

      (do (usage) (exit 1)))))
