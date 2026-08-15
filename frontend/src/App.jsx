import { useState } from 'react'

function App() {
  const [originalUrl, setOriginalUrl] = useState('')
  const [customAlias, setCustomAlias] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [copied, setCopied] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)
    setError(null)
    setResult(null)
    setCopied(false)

    try {
      const response = await fetch('http://localhost:8081/api/urls/shorten', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ originalUrl, customAlias }),
      });

      let data;
      try {
        data = await response.json();
      } catch (err) {
        throw new Error('Server returned an invalid response.');
      }

      if (!response.ok) {
        if (data.error) throw new Error(data.error); // Handled backend errors
        if (data.originalUrl) throw new Error(data.originalUrl); // Validation error
        throw new Error('Failed to shorten URL');
      }

      setResult(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false)
    }
  }

  const handleCopy = () => {
    if (result && result.shortUrl) {
      navigator.clipboard.writeText(result.shortUrl)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  return (
    <div className="glass-card">
      <h1 className="title">Antigravity Links</h1>
      <p className="subtitle">Shorten your URLs instantly with a premium experience.</p>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="input-group">
          <input
            type="url"
            className="input-field"
            placeholder="Paste your long URL here... (e.g., https://example.com/very/long/path)"
            value={originalUrl}
            onChange={(e) => setOriginalUrl(e.target.value)}
            required
          />
          <input
            type="text"
            className="input-field"
            placeholder="Custom alias (optional)"
            value={customAlias}
            onChange={(e) => setCustomAlias(e.target.value)}
            pattern="[a-zA-Z0-9-]+"
            title="Only letters, numbers, and hyphens are allowed for custom aliases."
          />
        </div>
        
        <button type="submit" className="btn-primary" disabled={isLoading || !originalUrl}>
          {isLoading ? 'Shortening...' : 'Shorten URL'}
        </button>
      </form>

      {result && (
        <div className="result-container">
          <h3>Your short URL is ready!</h3>
          <div className="short-url-box">
            <a href={result.shortUrl} target="_blank" rel="noopener noreferrer" className="short-url-link">
              {result.shortUrl}
            </a>
            <button className="copy-btn" onClick={handleCopy}>
              {copied ? 'Copied!' : 'Copy'}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
