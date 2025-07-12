import { useState } from 'react'

function MessageInput({ onSendMessage, isConnected }) {
  const [currentMessage, setCurrentMessage] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!currentMessage.trim() || !isConnected) return

    onSendMessage(currentMessage)
    setCurrentMessage('')
  }

  return (
    <div className="bg-white border-top p-3">
      <form onSubmit={handleSubmit}>
        <div className="input-group">
          <input
            type="text"
            className="form-control"
            value={currentMessage}
            onChange={(e) => setCurrentMessage(e.target.value)}
            placeholder="Type your message..."
            disabled={!isConnected}
          />
          <button 
            className="btn btn-primary" 
            type="submit"
            disabled={!isConnected || !currentMessage.trim()}
          >
            Send
          </button>
        </div>
      </form>
    </div>
  )
}

export default MessageInput