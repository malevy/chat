import { useState } from "react";

function JoinDialog({ onJoin }) {
    const [username, setUsername] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();
        if (username.trim()) {
            onJoin(username.trim());
        }
    };

    return (
        <div className="container-fluid vh-100 d-flex align-items-center justify-content-center bg-light">
            <div className="card shadow" style={{ width: "400px" }}>
                <div className="card-body">
                    <h4 className="card-title text-center mb-4">Join Chat</h4>
                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label htmlFor="username" className="form-label">
                                Username
                            </label>
                            <input
                                type="text"
                                className="form-control"
                                id="username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder="Enter your username"
                                required
                            />
                        </div>
                        <button type="submit" className="btn btn-primary w-100">
                            Join
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default JoinDialog;
