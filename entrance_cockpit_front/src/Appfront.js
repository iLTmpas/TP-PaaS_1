import React, { useEffect, useState } from 'react';

function App() {
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    const ws = new WebSocket('ws://172.31.249.225:8086/ws');

    ws.onopen = () => {
      console.log("WebSocket connecté !");
      ws.send("Hello du front !");
    };

    ws.onmessage = (event) => {
      console.log("Message reçu :", event.data);

      let data;
      try {
        data = JSON.parse(event.data);
      } catch (e) {
        console.error("Message non JSON :", event.data);
        return;
      }

      setLogs((prev) => [data, ...prev]);
    };

    return () => ws.close();
  }, []);

  const handleRedirect = () => {
    window.location.href = "http://172.31.252.93:9191/employes";
  }

  return (
    <div style={{ padding: "20px" }}>
      <h1>📡 Logs en direct</h1>

      <button 
        onClick={handleRedirect} 
        style={{ marginBottom: 20, padding: '10px 20px', cursor: 'pointer' }}
      >
        Aller aux employés
      </button>

      <div style={{ marginTop: 20 }}>
        {/* Affichage par paires (2 colonnes). Le premier log va à gauche, le second à droite, etc. */}
        {(() => {
          const rows = [];
          for (let i = 0; i < logs.length; i += 2) {
            rows.push([logs[i], logs[i + 1] || null]);
          }

          const extractNumberFromPayload = (payload) => {
            if (typeof payload !== 'string') return null;
            // Cherche un nombre après ':' ou '=' (ex: "temp: 23" ou "val=42")
            const m = payload.match(/[:=]\s*(-?\d+(?:\.\d+)?)/);
            return m ? m[1] : null;
          };

          const renderLog = (log, key) => {
            if (!log) return null;

            let bgColor = "#fff";

            const extracted = extractNumberFromPayload(log.payload);
            let display = extracted !== null ? extracted : log.payload;

            if (log.type === "attempt_log") {
              if (typeof log.payload === "string" && /refusé/i.test(log.payload)) {
                bgColor = "#ffd6d6"; // rouge clair
                display = log.payload;
              } else {
                bgColor = "#ececec"; // gris normal
                display = "le badge " + display + "tente de rentrer";
              }
            } else if (log.type === "entrance_log") {
              bgColor = "#d4ffd4"; // vert
            }


            return (
              <div key={key} style={{
                padding: 10,
                margin: 5,
                borderRadius: 8,
                background: bgColor,
                boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
              }}>
                {log.type === "attempt_log" && (
                  <div>
                    <strong>🕒 Tentative :</strong> {display}
                  </div>
                )}

                {log.type === "entrance_log" && (
                  <div>
                    <strong>✅ Accès :</strong> {display}
                  </div>
                )}
              </div>
            );
          };

          return (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {rows.map((pair, rIdx) => (
                <div key={rIdx} style={{ display: 'flex', flexWrap: 'wrap' }}>
                  <div style={{ flex: '1 1 50%', boxSizing: 'border-box' }}>
                    {renderLog(pair[1], `l-${rIdx}-0`)}
                  </div>
                  <div style={{ flex: '1 1 50%', boxSizing: 'border-box' }}>
                    {renderLog(pair[0], `l-${rIdx}-1`)}
                  </div>
                </div>
              ))}
            </div>
          );
        })()}
      </div>
    </div>
  );
}

export default App;
