
# ClearMeet

ClearMeet turns a meeting recording into a transcript and an actionable AI summary. The React frontend uploads audio to a Spring Boot REST API, which uses Groq for transcription and summarization.
## Stack

- React 19 and Vite
- Axios for frontend API requests
- Spring Boot 3.3 and Java 17
- Groq OpenAI-compatible API
- `whisper-large-v3-turbo` for transcription
- `openai/gpt-oss-120b` for summarization

## Requirements

- Node.js 18 or newer
- Java 17 or newer
- A Groq API key
- Maven Wrapper support, included in `SpringAiProject`

Video:  



## Configuration

Set the Groq API key as an environment variable. In PowerShell:

```powershell
$env:GROQ_API_KEY = "your-groq-api-key"
```

The backend reads this variable from `SpringAiProject/src/main/resources/application.properties`. Do not commit the key to the repository.

The configured chat model must be available to your Groq account. To list models available to the current key:

```powershell
Invoke-RestMethod `
	-Uri "https://api.groq.com/openai/v1/models" `
	-Headers @{ Authorization = "Bearer $env:GROQ_API_KEY" } |
	Select-Object -ExpandProperty data |
	Select-Object id
```

If necessary, replace `spring.ai.openai.chat.model` in `SpringAiProject/src/main/resources/application.properties` with one of the returned chat model IDs.

## Run Locally

Open two terminals from the workspace root.

### 1. Start the backend

```powershell
Set-Location .\SpringAiProject
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8080`.

### 2. Start the frontend

```powershell
Set-Location .\Metting-Summariser
npm install
npm run dev
```

Vite normally starts at `http://localhost:5173`. If that port is busy, Vite selects the next available port and prints the URL in the terminal.

The Vite proxy forwards frontend requests from `/api` to `http://localhost:8080`, so no frontend API URL needs to be configured.

## Using the App

1. Open the Vite URL in a browser.
2. Drop an audio file onto the upload area, or click it to browse.
3. Optionally enter a meeting title.
4. Select **Generate meeting notes**.
5. Review the generated summary and transcript.

Supported uploads are audio files up to 25 MB, including MP3, WAV, and M4A files.

## API

### Health check

```http
GET /api/meetings/health
```

### Generate a summary

```http
POST /api/meetings/summarize
Content-Type: multipart/form-data
```

Form fields:

- `file`: required audio file
- `title`: optional meeting title

Successful response:

```json
{
	"title": "Product strategy sync",
	"fileName": "meeting.m4a",
	"transcript": "The complete transcription...",
	"summary": "The actionable AI summary..."
}
```

Example with PowerShell:

```powershell
curl.exe -X POST http://localhost:8080/api/meetings/summarize `
	-F "file=@C:\path\to\meeting.m4a" `
	-F "title=Product strategy sync"
```

## Development Checks

Frontend:

```powershell
Set-Location .\Metting-Summariser
npm run lint
npm run build
```

Backend:

```powershell
Set-Location .\SpringAiProject
.\mvnw.cmd test
```

## Troubleshooting

### `OpenAI/Groq API key is not configured`

Set `GROQ_API_KEY` in the same terminal that starts the backend, then restart Spring Boot.

### `model_not_found`

The API key cannot use the configured chat model. Run the model-list command above and update `spring.ai.openai.chat.model` with an available chat model.

### `Failed to transcribe the uploaded audio file`

Check that the file is a supported audio format, is smaller than 25 MB, and that `whisper-large-v3-turbo` is available to your Groq account.

### Frontend cannot reach the backend

Confirm the backend health endpoint returns a response:

```powershell
Invoke-WebRequest http://localhost:8080/api/meetings/health
```


