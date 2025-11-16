const API_URL = "http://172.31.250.252:8086/api/entrance";

export default {
    authorize: async (badgeId) => {
        const res = await fetch(`${API_URL}/authorize/${badgeId}`, {
            method: "POST"
        });
        return await res.text();
    },

    openDoor: async () => {
        await fetch(`${API_URL}/open`, { method: "POST" });
    }
};
