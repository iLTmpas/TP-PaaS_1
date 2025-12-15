import React, { useEffect, useState, useRef } from "react";

export default function LiveLogs() {
  const [logs, setLogs] = useState([]);
  const logsContainerRef = useRef(null);

  useEffect(() => {
    const ws = new WebSocket("ws://172.31.249.225:8086/ws");

    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      let newLog = null;

      const logBase = {
        id: Date.now() + Math.random(),
      };

      // ---------- ATTEMPT LOG ----------
      if (data.type === "attempt_log") {
        let payload = data.payload;
        if (typeof payload === "string") {
          try {
            payload = JSON.parse(payload);
          } catch (e) {

          }
        }

        if (payload && payload.badgeId !== undefined) {
          newLog = {
            ...logBase,
            text: `🕒 Tentative du numéro de badge ${payload.badgeId}`,
            color: "#888",
          };
        }
        else if (typeof data.payload === "string" && data.payload.includes("refusé")) {
          newLog = {
            ...logBase,
            text: `❌ ${data.payload}`,
            color: "red",
          };
        }
      }

      // ---------- ENTRANCE LOG ----------
      else if (data.type === "entrance_log") {
        let payload = data.payload;

        if (typeof payload === "string") {
          try {
            payload = JSON.parse(payload);
          } catch (e) {}
        }

        const badgeId =
          payload && payload.badgeId !== undefined ? payload.badgeId : payload;

        newLog = {
          ...logBase,
          text: `✅ Accès du numéro de badge ${badgeId}`,
          color: "green",
        };
      }

      if (newLog) {
        setLogs((prev) => [...prev, newLog]);
      }
    };

    return () => ws.close();
  }, []);

  useEffect(() => {
    if (logsContainerRef.current) {
      logsContainerRef.current.scrollTop =
        logsContainerRef.current.scrollHeight;
    }
  }, [logs]);

  const logStyle = (color) => ({
    padding: 10,
    marginBottom: 8,
    borderRadius: 5,
    background:
      color === "green"
        ? "#e6ffe6"
        : color === "red"
        ? "#ffe6e6"
        : "#f5f5f5",
    borderLeft: `5px solid ${color}`,
    fontWeight: "600",
    color: "#333",
  });

  return (
    <div style={{ padding: 15 }}>
      <h2>📡 Logs en direct (Nouveau en Bas)</h2>

      <div
        ref={logsContainerRef}
        style={{
          maxHeight: 300,
          overflowY: "auto",
          border: "1px solid #ddd",
          padding: 5,
        }}
      >
        {logs.map((log) => (
          <div key={log.id} style={logStyle(log.color)}>
            {log.text}
          </div>
        ))}
      </div>

      {logs.length === 0 && (
        <p style={{ textAlign: "center", marginTop: 20, color: "#999" }}>
          En attente des données de la WebSocket...
        </p>
      )}
    </div>
  );
}
