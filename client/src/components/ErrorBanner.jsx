function ErrorBanner({ onDismiss }) {

  return (
    <div className="alert alert-danger alert-dismissible mb-0" role="alert">
      <strong>Connection Error:</strong> Unable to connect to the chat server. Please try again.
      <button 
        type="button" 
        className="btn-close" 
        aria-label="Close"
        onClick={onDismiss}
      ></button>
    </div>
  )
}

export default ErrorBanner