const messagesEl = document.getElementById("messages");
const form = document.getElementById("chat-form");
const promptEl = document.getElementById("prompt");
const sendBtn = document.getElementById("send");
const healthEl = document.getElementById("health");

/** @type {{ role: string, content: string }[]} */
const history = [];

function appendBubble(role, content) {
  const div = document.createElement("div");
  div.className = `bubble ${role === "user" ? "user" : "assistant"}`;
  div.textContent = content;
  messagesEl.appendChild(div);
  messagesEl.scrollTop = messagesEl.scrollHeight;
}

async function refreshHealth() {
  try {
    const res = await fetch("/api/health");
    const data = await res.json();
    if (data.ollamaReachable) {
      healthEl.textContent = `Ollama OK — ${data.defaultModel} @ ${data.baseUrl}`;
      healthEl.className = "health ok";
    } else {
      healthEl.textContent = "Ollama に未接続（clojure -M:run up などで起動）";
      healthEl.className = "health ng";
    }
  } catch {
    healthEl.textContent = "API に接続できません";
    healthEl.className = "health ng";
  }
}

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  const prompt = promptEl.value.trim();
  if (!prompt) return;

  promptEl.value = "";
  sendBtn.disabled = true;

  history.push({ role: "user", content: prompt });
  appendBubble("user", prompt);

  try {
    const res = await fetch("/api/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ messages: history }),
    });

    const data = await res.json();
    if (!res.ok) {
      const err = data.detail || data.title || data.error || "エラー";
      appendBubble("assistant", String(err));
      return;
    }

    history.push({ role: "assistant", content: data.content });
    appendBubble("assistant", data.content);
  } catch (err) {
    appendBubble("assistant", `通信エラー: ${err}`);
  } finally {
    sendBtn.disabled = false;
    promptEl.focus();
  }
});

refreshHealth();
setInterval(refreshHealth, 15000);
