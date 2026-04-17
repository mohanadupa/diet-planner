from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import numpy as np, cv2, os, tempfile
from pathlib import Path
import sys

# Ensure the project root is in sys.path for importing the verifier
sys.path.append(str(Path(__file__).resolve().parents[2]))
from verify_setup import ImprovedSignatureVerifier

app = FastAPI()

# Allow requests from the frontend (adjust origins as needed)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize a single verifier instance (stateless per request)
verifier = ImprovedSignatureVerifier()

@app.post("/verify")
async def verify_signature(
    genuine_image: UploadFile = File(...),
    test_image: UploadFile = File(...),
    sensitivity: float = 1.0,
):
    # Helper to write uploaded file to a temporary location
    def save_temp(upload: UploadFile) -> Path:
        suffix = Path(upload.filename).suffix or ".png"
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            content = upload.file.read()
            tmp.write(content)
            return Path(tmp.name)

    # Save both images
    genuine_path = save_temp(genuine_image)
    test_path = save_temp(test_image)

    try:
        # Load genuine signature into the verifier
        verifier.set_genuine_signature(str(genuine_path))
        # Perform verification on the test image
        result = verifier.verify_signature(str(test_path))
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc))
    finally:
        # Clean up temporary files
        for p in (genuine_path, test_path):
            try:
                os.remove(p)
            except OSError:
                pass

    # Construct the response with a metrics sub-dictionary
    response_body = {
        "filename": result.get("filename"),
        "status": result.get("status"),
        "is_genuine": result.get("is_genuine"),
        "metrics": result, # Assuming 'result' itself contains the detailed metrics
        "sensitivity_used": sensitivity,
        "visualization": ""  # placeholder for frontend image
    }
    return JSONResponse(content=response_body)
