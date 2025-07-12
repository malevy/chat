import { useState, useRef, useCallback } from "react";

export function useWebSocket() {
    const [isConnected, setIsConnected] = useState(false);
    const [messages, setMessages] = useState([]);
    const [error, setError] = useState(null);
    const socketRef = useRef(null);

    const connect = useCallback(
        (username) => {
            if (!username?.trim() || isConnected) return;

            setError(null);
            const ws = new WebSocket(
                `${import.meta.env.VITE_WEBSOCKET_URL}?username=${username}`
            );

            ws.onopen = () => {
                console.log("connection open");
                setIsConnected(true);
                socketRef.current = ws;
            };

            ws.onmessage = (event) => {
                const data = JSON.parse(event.data);
                const newMessage = {
                    id: data.id,
                    type: data.type,
                    message: data.message,
                    timestamp: new Date(data.timestamp),
                    username: data.username,
                };
                console.log(newMessage);
                setMessages((prev) => [...prev, newMessage]);
            };

            ws.onclose = () => {
                setIsConnected(false);
                socketRef.current = null;
                console.log("connection close");
            };

            ws.onerror = (errorEvent) => {
                console.error("WebSocket error:", errorEvent);
                setIsConnected(false);
                setError(errorEvent);
            };
        },
        [isConnected]
    );

    const disconnect = useCallback(() => {
        if (socketRef.current) {
            socketRef.current.close();
        }
        setIsConnected(false);
        setMessages([]);
        setError(null);
        socketRef.current = null;
    }, []);

    const send = useCallback(
        (message) => {
            if (!message?.trim() || !socketRef.current || !isConnected) return;

            // id and timestamp are set by the server
            socketRef.current.send(
                JSON.stringify({
                    type: "message",
                    message: message,
                })
            );
        },
        [isConnected]
    );

    return {
        isConnected,
        messages,
        error,
        connect,
        disconnect,
        send,
    };
}
