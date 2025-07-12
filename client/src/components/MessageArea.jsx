import { useEffect, useRef } from 'react'

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
            <div className={`d-flex ${msg.username === currentUsername ? 'justify-content-end' : 'justify-content-start'}`}>
              <div className={`card ${msg.username === currentUsername ? 'bg-primary text-white' : 'bg-white'}`} style={{ maxWidth: '70%' }}>
                <div className="card-body py-2 px-3">
                  {msg.username !== currentUsername && (
                    <small className="text-muted fw-bold">{msg.username}</small>
                  )}
                  <div>{msg.message}</div>
                  <small className={`${msg.username === currentUsername ? 'text-white-50' : 'text-muted'}`}>
                    {msg.timestamp.toLocaleTimeString()}
                  </small>
                </div>
              </div>
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
    </div>
  )
}

export default MessageArea