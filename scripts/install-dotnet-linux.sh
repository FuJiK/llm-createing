#!/usr/bin/env bash
# Ubuntu / Debian 向け: dotnet-host だけだと
#   Error: [/usr/lib/dotnet/host/fxr] does not exist
# になるため、Microsoft 公式リポジトリから SDK を入れる。
set -euo pipefail

SDK_MAJOR="${1:-8}"

if ! command -v wget >/dev/null 2>&1; then
  echo "wget が必要です: sudo apt install wget"
  exit 1
fi

. /etc/os-release
case "${ID:-}" in
  ubuntu)
    PACKAGE="dotnet-sdk-${SDK_MAJOR}"
    ;;
  debian)
    PACKAGE="dotnet-sdk-${SDK_MAJOR}.0"
    ;;
  *)
    echo "未対応のディストリビューション: ${ID:-unknown}"
    echo "手動: https://learn.microsoft.com/dotnet/core/install/linux"
    exit 1
    ;;
esac

echo "==> Microsoft パッケージリポジトリを登録"
wget -q "https://packages.microsoft.com/config/${ID}/${VERSION_ID}/packages-microsoft-prod.deb" -O /tmp/packages-microsoft-prod.deb
sudo dpkg -i /tmp/packages-microsoft-prod.deb
rm -f /tmp/packages-microsoft-prod.deb

echo "==> ${PACKAGE} をインストール"
sudo apt-get update
sudo apt-get install -y "${PACKAGE}"

echo "==> 確認"
dotnet --info

echo
echo "完了。web アプリ:"
echo "  cd web/LlmCreating.Web && dotnet run"
