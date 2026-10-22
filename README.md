# llm-createing

Docker で **Ollama** を動かし、**Clojure** からローカル LLM を起動・モデル管理・推論するための最小構成です。

「LLM をローカルに作成する」= 自前で学習するのではなく、**コンテナ上に推論環境を立て、モデルを pull して使う**想定です（一般的なローカル LLM 運用と同じです）。

## 前提

- [Docker](https://docs.docker.com/get-docker/)（Compose v2）
- [Clojure CLI](https://clojure.org/guides/install_clojure)（Java 17+）— LLM スタック管理用
- [.NET 8 SDK](https://dotnet.microsoft.com/download) — Web UI 用（任意）

## クイックスタート

```bash
cp .env.example .env

# Ollama 起動
clj -M:run up

# 疎通確認
clj -M:run health

# 小さめモデルを取得（初回はダウンロードに時間がかかります）
clj -M:run pull llama3.2:1b

# 推論
clj -M:run generate "Clojure で REPL を起動するコマンドは？"
clj -M:run chat "こんにちは。自己紹介してください。"
```

停止:

```bash
clj -M:run down
```

## 構成

| パス | 役割 |
|------|------|
| `docker-compose.yml` | Ollama コンテナ・ボリューム |
| `resources/config.edn` | デフォルト URL・モデル名 |
| `src/llm_creating/docker.clj` | `docker compose` 操作 |
| `src/llm_creating/ollama.clj` | Ollama HTTP API |
| `src/llm_creating/api.clj` | アプリ組み込み用ファサード |
| `src/llm_creating/main.clj` | CLI |

環境変数（`.env` またはシェル）:

- `OLLAMA_BASE_URL` — API（既定: `http://localhost:11434`）
- `OLLAMA_DEFAULT_MODEL` — 既定モデル（例: `llama3.2:1b`）
- `OLLAMA_HOST_PORT` — ホスト側ポート

## C# Web アプリ（ローカル UI + バックエンド）

ブラウザでチャットし、ASP.NET Core が **Ollama** にプロキシします。

```bash
# 1. Ollama を起動（上記クイックスタート）
clojure -M:run up
clojure -M:run pull llama3.2:1b

# 2. .NET SDK（初回のみ — dotnet-host だけでは動きません）
chmod +x scripts/install-dotnet-linux.sh
./scripts/install-dotnet-linux.sh 8

# 3. Web アプリ
cd web/LlmCreating.Web
dotnet run
```

ブラウザで `http://localhost:5236` を開きます（`Properties/launchSettings.json` の URL）。

`Error: [/usr/lib/dotnet/host/fxr] does not exist` が出たら → [web/LlmCreating.Web/README.md](web/LlmCreating.Web/README.md) を参照。

| エンドポイント | 説明 |
|----------------|------|
| `GET /` | チャット UI |
| `GET /api/health` | Ollama 疎通 |
| `POST /api/chat` | `{ "prompt": "..." }` または `{ "messages": [...] }` |

`appsettings.json` の `Ollama:BaseUrl` / `DefaultModel` で接続先を変更できます。

## REPL から使う例

```clojure
(require '[llm-creating.api :as llm])

(llm/start-stack!)
(llm/ask "Docker で LLM を動かすメリットを3つ")
(llm/stop-stack!)
```

## テスト

```bash
clj -M:test
```

## GPU

GPU を使う場合は `docker-compose.yml` の `ollama` サービスに、環境に合わせて [Ollama の Docker ドキュメント](https://github.com/ollama/ollama/blob/main/docs/docker.md) の `deploy.resources.reservations.devices` などを追加してください。

## 注意

- モデルは初回 `pull` で Hugging Face 等から取得します（容量・時間に注意）。
- 本リポジトリは推論インフラの骨組みです。RAG・fine-tuning・独自モデル学習は別途スタックが必要です。
