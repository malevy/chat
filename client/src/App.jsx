import { useState } from "react";
import "./App.css";
import ErrorBanner from "./components/ErrorBanner";
import Header from "./components/Header";
import JoinDialog from "./components/JoinDialog";
import MessageArea from "./components/MessageArea";
import MessageInput from "./components/MessageInput";
import { useWebSocket } from "./hooks/useWebSocket";

function App() {
    const [username, setUsername] = useState("");
    const { isConnected, messages, error, connect, disconnect, send } =
        useWebSocket();

    const handleJoin = (username) => {
        setUsername(username);
        connect(username);
    };

    const handleDisconnect = () => {
        disconnect();
    };

    const handleErrorDismiss = () => {
        // Clear error by reconnecting - this will reset the error state
        if (username) {
            connect(username);
        }
    };

    if (error) {
        <ErrorBanner onDismiss={handleErrorDismiss} />;
    }

    if (!isConnected) {
        return <JoinDialog onJoin={handleJoin} />;
    }

    return (
        <div className="container-fluid vh-100 d-flex flex-column">
            <Header username={username} onDisconnect={handleDisconnect} />
            <MessageArea messages={messages} currentUsername={username} />
            <MessageInput onSendMessage={send} isConnected={isConnected} />
        </div>
    );
}

export default App;
