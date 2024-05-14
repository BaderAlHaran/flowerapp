from flask import Flask, request, jsonify
from ultralytics import YOLO
import base64
from io import BytesIO
from PIL import Image

app = Flask(__name__)

# Load the YOLO models
model_diseases = YOLO('Plant_Diseases.pt')
model_soil = YOLO('Soil_Type.pt')

@app.route('/predict_diseases', methods=['POST'])
def predict_diseases():
    try:
        data = request.get_json()
        image_b64 = data['image']
        image_bytes = base64.b64decode(image_b64)
        image = Image.open(BytesIO(image_bytes))
        results = model_diseases.predict(image)
        boxes = [result.boxes for result in results]  # Collect all boxes for each result
        response_data = str(boxes)
    except Exception as e:
        response_data = {'error': str(e)}
    return jsonify({"result": response_data})

@app.route('/predict_soil', methods=['POST'])
def predict_soil():
    try:
        data = request.get_json()
        image_b64 = data['image']
        image_bytes = base64.b64decode(image_b64)
        image = Image.open(BytesIO(image_bytes))
        results = model_soil.predict(image)
        boxes = [result.boxes for result in results]  # Collect all boxes for each result
        response_data = str(boxes)
    except Exception as e:
        response_data = {'error': str(e)}
    return jsonify({"result": response_data})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9321)