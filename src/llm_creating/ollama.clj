(ns llm-creating.ollama
  "Ollama HTTP API クライアント（ローカル LLM）。"
  (:require [cheshire.core :as json]
            [clojure.string :as str])
  (:import [java.net URI]
           [java.net.http HttpClient HttpRequest HttpResponse]
           [java.time Duration]
           [java.nio.charset StandardCharsets]))

(defn- uri [base-url path]
  (URI/create (str (str/replace base-url #"/$" "") path)))

(defn- request
  [{:keys [base-url method path body timeout-ms]}]
  (let [client (.build (HttpClient/newBuilder))
        builder (HttpRequest/newBuilder
                 (.uri (uri base-url path))
                 (.timeout (Duration/ofMillis timeout-ms))
                 (.header "Content-Type" "application/json")
                 (.header "Accept" "application/json"))
        req (if body
              (.POST builder
                     (HttpRequest/BodyPublishers/ofString
                      (json/generate-string body)
                      StandardCharsets/UTF_8))
              (.GET builder))
        resp (.send client req (HttpResponse/BodyHandlers/ofString))]
    {:status (.statusCode resp)
     :body (.body resp)}))

(defn- parse-json [s]
  (when (and s (not (str/blank? s)))
    (json/parse-string s true)))

(defn health?
  "Ollama が応答するか。"
  [{:keys [base-url request-timeout-ms]}]
  (try
    (let [{:keys [status body]} (request {:base-url base-url
                                          :method :get
                                          :path "/api/tags"
                                          :timeout-ms (or request-timeout-ms 5000)})]
      (and (= 200 status) (map? (parse-json body))))
    (catch Exception _ false)))

(defn list-models
  [{:keys [base-url request-timeout-ms]}]
  (let [{:keys [status body]} (request {:base-url base-url
                                        :method :get
                                        :path "/api/tags"
                                        :timeout-ms request-timeout-ms})]
    (when (= 200 status)
      (:models (parse-json body)))))

(defn pull-model!
  "モデルをダウンロードする（初回は時間がかかる）。"
  [{:keys [base-url request-timeout-ms]} model]
  (let [{:keys [status body]} (request {:base-url base-url
                                        :method :post
                                        :path "/api/pull"
                                        :body {:name model :stream false}
                                        :timeout-ms request-timeout-ms})]
    (when (= 200 status)
      (parse-json body))))

(defn generate
  "プロンプトを送り、生成テキストを返す。"
  [{:keys [base-url request-timeout-ms]} {:keys [model prompt system stream]
                                           :or {stream false}}]
  (let [payload (cond-> {:model model :prompt prompt :stream stream}
                  system (assoc :system system))
        {:keys [status body]} (request {:base-url base-url
                                        :method :post
                                        :path "/api/generate"
                                        :body payload
                                        :timeout-ms request-timeout-ms})]
    (when (= 200 status)
      (:response (parse-json body)))))

(defn chat
  "チャット形式（messages）で応答テキストを返す。"
  [{:keys [base-url request-timeout-ms]} {:keys [model messages stream]
                                          :or {stream false}}]
  (let [{:keys [status body]} (request {:base-url base-url
                                        :method :post
                                        :path "/api/chat"
                                        :body {:model model :messages messages :stream stream}
                                        :timeout-ms request-timeout-ms})]
    (when (= 200 status)
      (get-in (parse-json body) [:message :content]))))
