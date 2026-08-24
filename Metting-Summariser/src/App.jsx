import { useRef, useState } from 'react'
import axios from 'axios'
import './App.css'

function App() {
  const [file, setFile] = useState(null)
  const [title, setTitle] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [isDragging, setIsDragging] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const fileInputRef = useRef(null)

  const chooseFile = (nextFile) => {
    if (!nextFile) return
    if (!nextFile.type.startsWith('audio/')) {
      setError('Please choose an audio file.')
      return
    }
    setFile(nextFile)
    setError('')
    setResult(null)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!file) {
      setError('Add a meeting recording before generating a summary.')
      return
    }

    const formData = new FormData()
    formData.append('file', file)
    if (title.trim()) formData.append('title', title.trim())

    setIsLoading(true)
    setError('')
    try {
      const response = await axios.post('/api/meetings/summarize', formData)
      setResult(response.data)
    } catch (requestError) {
      setError(
        requestError.response?.data?.message ||
          'The recording could not be processed. Check that the backend is running and try again.',
      )
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label="ClearMeet home">
          <span className="brand-mark">CM</span>
          <span>ClearMeet</span>
        </a>
        <span className="status"><span className="status-dot" /> AI meeting notes</span>
      </header>

      <section className="intro">
        <p className="eyebrow">RECORDING TO RECAP</p>
        <h1>Give every meeting<br /><em>a second life.</em></h1>
        <p className="lede">Upload a recording and get a clear, actionable summary in moments.</p>
      </section>

      <div className="workspace">
        <form className="upload-panel" onSubmit={handleSubmit}>
          <div className="panel-heading">
            <div><span className="step-number">01</span><h2>Bring in a recording</h2></div>
            <span className="file-limit">MP3, WAV, M4A · 25 MB max</span>
          </div>

          <input ref={fileInputRef} className="visually-hidden" type="file" accept="audio/*" onChange={(event) => chooseFile(event.target.files[0])} />
          <button
            type="button"
            className={`dropzone ${isDragging ? 'is-dragging' : ''} ${file ? 'has-file' : ''}`}
            onClick={() => fileInputRef.current?.click()}
            onDragOver={(event) => { event.preventDefault(); setIsDragging(true) }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={(event) => { event.preventDefault(); setIsDragging(false); chooseFile(event.dataTransfer.files[0]) }}
          >
            <span className="upload-icon">↑</span>
            <span className="dropzone-copy">
              <strong>{file ? file.name : 'Drop your audio here'}</strong>
              <small>{file ? `${(file.size / 1024 / 1024).toFixed(2)} MB · Ready to transcribe` : 'or click to browse your files'}</small>
            </span>
            {file && <span className="remove-file" onClick={(event) => { event.stopPropagation(); setFile(null) }} aria-label="Remove file">×</span>}
          </button>

          <label className="field-label" htmlFor="meeting-title">Meeting title <span>optional</span></label>
          <input id="meeting-title" className="title-input" value={title} onChange={(event) => setTitle(event.target.value)} placeholder="e.g. Product strategy sync" />
          {error && <p className="error-message" role="alert">{error}</p>}
          <button className="submit-button" type="submit" disabled={isLoading}>
            {isLoading ? <><span className="spinner" /> Processing recording...</> : <>Generate meeting notes <span>→</span></>}
          </button>
          <p className="privacy-note">Your recording is sent securely to the meeting assistant.</p>
        </form>

        <aside className="side-note">
          <span className="note-line" />
          <p>Make space for<br /><strong>what matters next.</strong></p>
          <span className="note-symbol">✳</span>
        </aside>
      </div>

      {result && <section className="results" aria-live="polite">
        <div className="results-heading"><div><span className="step-number">02</span><h2>{result.title}</h2></div><span className="complete-badge">Complete</span></div>
        <div className="result-grid">
          <article><p className="result-label">AI SUMMARY</p><div className="result-text">{result.summary}</div></article>
          <article><p className="result-label">TRANSCRIPT</p><div className="result-text transcript">{result.transcript}</div></article>
        </div>
      </section>}
      <footer><span>ClearMeet</span><span>Turn conversations into momentum.</span></footer>
    </main>
  )
}

export default App
