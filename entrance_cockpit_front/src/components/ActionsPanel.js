import { useState } from "react";
import api from "../services/api";

export default function ActionsPanel() {
    const [badgeId, setBadgeId] = useState("");
    const [result, setResult] = useState(null);

    const authorize = async () => {
        const res = await api.authorize(badgeId);
        setResult(res);
    };

    const openDoor = async () => {
        await api.openDoor();
        alert("🚪 Porte ouverte manuellement !");
    };

    return (
        <div style={{marginTop: 20}}>
            <h2>🎛 Actions</h2>

            <input
                type="number"
                placeholder="Badge ID"
                value={badgeId}
                onChange={(e) => setBadgeId(e.target.value)}
            />

            <button onClick={authorize}>Vérifier badge</button>

            <button onClick={openDoor} style={{marginLeft: 10}}>
                Ouvrir la porte
            </button>

            {result && (
                <p style={{marginTop: 10}}>
                    Résultat : <strong>{result}</strong>
                </p>
            )}
        </div>
    );
}
