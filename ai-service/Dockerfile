FROM python:3.12-slim-bullseye

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Install requirements
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY app.py .

# Start with Gunicorn using the configured service port
CMD ["sh", "-c", "gunicorn --bind 0.0.0.0:${AI_SERVICE_PORT:-5000} app:app"]
