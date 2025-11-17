import React, { useEffect, useState } from "react";

export default function LiveLogs() {
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    const ws = new WebSocket("ws://172.31.249.225:8086/ws");

    ws.onmessage = (event) => {
        const raw = event.data;
        const data = JSON.parse(raw);
      
        let newLog = null;

        if (data.type === "attempt_log") {
            const payload = data.payload;

            // Cas 1 : tentative normale → payload = { badgeId: 1 }
            if (payload.badgeId !== undefined) {
                newLog = {
                    text: `🕒 Tentative : badge ${payload.badgeId}`,
                    color: "#888"
                };
            }

            // Cas 2 : accès refusé → payload = "Accès refusé badge=2"
            else if (typeof payload === "string" && payload.includes("refusé")) {
                newLog = {
                    text: `❌ ${payload}`,
                    color: "red"
                };
            }
        }

        // Cas 3 : accès autorisé
        else if (data.type === "entrance_log") {
            newLog = {
                text: `🟢 ${data.payload}`,
                color: "green"
            };
        }

        if (newLog) {
            setLogs(prev => [newLog, ...prev]); // ajouter en haut
        }
    };

    return () => ws.close();
  }, []);

  return (
    <div style={{ padding: 15 }}>
      <h2>📡 Logs en direct</h2>

      <div style={{ maxHeight: 300, overflowY: "scroll" }}>
        {logs.map((log, i) => (
          <div key={i} style={{
            padding: 8,
            marginBottom: 5,
            borderRadius: 5,
            background: "#fafafa",
            borderLeft: `4px solid ${log.color}`,
            fontWeight: "bold"
          }}>
            {log.text}
          </div>
        ))}
      </div>
    </div>
  );
}
