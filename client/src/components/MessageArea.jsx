import { useEffect, useRef } from 'react'

function SystemMessage({ message }) {
  return (
    <div className="d-flex justify-content-center">
      <div className="text-muted small text-center">
        <em>{message.message}</em>
        <br />
        <small>{message.timestamp.toLocaleTimeString()}</small>
      </div>
    </div>
  )
}

function RegularMessage({ message, currentUsername }) {
  return (
    <div className={`d-flex ${message.username === currentUsername ? 'justify-content-end' : 'justify-content-start'}`}>
      <div className={`card ${message.username === currentUsername ? 'bg-primary text-white' : 'bg-white'}`} style={{ maxWidth: '70%' }}>
        <div className="card-body py-2 px-3">
          {message.username !== currentUsername && (
            <small className="text-muted fw-bold">{message.username}</small>
          )}
          <div>{message.message}</div>
          <small className={`${message.username === currentUsername ? 'text-white-50' : 'text-muted'}`}>
            {message.timestamp.toLocaleTimeString()}
          </small>
        </div>
      </div>
    </div>
  )
}

function MessageArea({ messages, currentUsername }) {
  const messagesEndRef = useRef(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  return (
    <div className="flex-grow-1 overflow-auto p-3" style={{ backgroundColor: '#f8f9fa' }}>
      <div className="messages-container">
        {messages.map((msg) => (
          <div key={msg.id} className="mb-2">
            {msg.type === 'system' ? (
              <SystemMessage message={msg} />
            ) : (
              <RegularMessage message={msg} currentUsername={currentUsername} />
            )}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
    </div>
  )
}

export default MessageArea