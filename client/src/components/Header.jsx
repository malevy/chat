function Header({ username, onDisconnect }) {
  return (
    <div className="bg-primary text-white p-3 d-flex justify-content-between align-items-center">
      <h4 className="mb-0">Chat Room</h4>
      <div className="d-flex align-items-center">
        <span className="me-3">Welcome, {username}!</span>
        <button className="btn btn-outline-light btn-sm" onClick={onDisconnect}>
          Disconnect
        </button>
      </div>
    </div>
  )
}

export default Header