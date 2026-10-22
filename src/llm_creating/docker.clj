(ns llm-creating.docker
  "docker compose で Ollama コンテナを起動・停止する。"
  (:require [clojure.java.shell :as shell]
            [clojure.string :as str]))

(defn- project-root []
  (System/getProperty "user.dir"))

(defn- run-compose [args]
  (let [{:keys [out err exit]} (apply shell/sh "docker" "compose" args
                                         :dir (project-root)
                                         :env (merge (into {} (System/getenv))
                                                     {"COMPOSE_FILE" "docker-compose.yml"}))]
    {:stdout (str/trim (or out ""))
     :stderr (str/trim (or err ""))
     :exit exit}))

(defn up!
  "Ollama をバックグラウンド起動する。"
  []
  (run-compose ["up" "-d"]))

(defn down!
  "Ollama コンテナを停止する。"
  []
  (run-compose ["down"]))

(defn ps
  "compose サービスの状態を表示する。"
  []
  (run-compose ["ps"]))

(defn logs
  "直近の Ollama ログを表示する。"
  [& {:keys [tail] :or {tail "100"}}]
  (run-compose ["logs" "--tail" (str tail) "ollama"]))

(defn docker-available?
  []
  (let [{:keys [exit]} (shell/sh "docker" "info" :dir (project-root))]
    (= exit 0)))
