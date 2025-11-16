import React, { useEffect, useState } from 'react';

function App() {
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    const ws = new WebSocket('ws://172.31.250.252:8086/ws');

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

      setLogs((prev) => [...prev, data]);
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
        {logs.map((log, index) => {
          let bgColor = "#fff";

          if (log.type === "attempt_log") {
            if (typeof log.payload === "string" && /refusé/i.test(log.payload)) {
              bgColor = "#ffd6d6"; // rouge clair
            } else {
              bgColor = "#ececec"; // gris normal
            }
          } else if (log.type === "entrance_log") {
            bgColor = "#d4ffd4"; // vert
          }

          return (
            <div key={index} style={{
              padding: 10,
              marginBottom: 10,
              borderRadius: 8,
              background: bgColor
            }}>
              {log.type === "attempt_log" && (
                <div>
                  <strong>🕒 Tentative :</strong> {log.payload}
                </div>
              )}

              {log.type === "entrance_log" && (
                <div>
                  <strong>✅ Accès :</strong> {log.payload}
                </div>
              )}
            </div>
          )
        })}
      </div>
    </div>
  );
}

export default App;
