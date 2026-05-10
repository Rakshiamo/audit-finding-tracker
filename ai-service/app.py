from flask import Flask, jsonify
import os

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy", "service": "ai-service"}), 200

if __name__ == '__main__':
    port = int(os.environ.get("AI_SERVICE_PORT", 5000))
    app.run(host='0.0.0.0', port=port)
