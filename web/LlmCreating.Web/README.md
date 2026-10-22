# LlmCreating.Web（C# ローカル UI）

## 起動

```bash
# リポジトリルートで Ollama を起動済みであること
clojure -M:run up
clojure -M:run pull llama3.2:1b

cd web/LlmCreating.Web
dotnet run
```

ブラウザ: **http://localhost:5236**

## `Error: [/usr/lib/dotnet/host/fxr] does not exist`

**原因:** `dotnet-host` だけ入っていて **SDK / ランタイムが未インストール**（Ubuntu の分割パッケージでよくある）。

**対処（Ubuntu / Debian）:**

```bash
# リポジトリルートから
chmod +x scripts/install-dotnet-linux.sh
./scripts/install-dotnet-linux.sh 8
```

手動の場合は [Linux に .NET をインストール](https://learn.microsoft.com/ja-jp/dotnet/core/install/linux) を参照し、**`dotnet-sdk-8.0`**（または 10.x SDK）を入れてください。`dotnet-host` のみでは動きません。

確認:

```bash
dotnet --info
dotnet --list-sdks
```

## Cursor / メモリ不足で IDE が落ちる

Ollama + Docker + IDE は RAM を多く使います。

- 小さいモデル（例: `llama3.2:1b`）を使う
- 使わないとき `clojure -M:run down` で Ollama を止める
- スワップを増やす、他アプリを閉じる

## 設定

`appsettings.json` の `Ollama:BaseUrl` / `DefaultModel` を変更。
