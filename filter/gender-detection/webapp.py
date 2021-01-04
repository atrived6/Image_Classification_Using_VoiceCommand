from flask import Flask,request
from werkzeug.utils import secure_filename
from detect_gender import detect_gender
app = Flask(__name__)



def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ['jpg','jpeg']

@app.route('/gender',methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        if 'file' not in request.files:
            return "Please upload a file with key type as file and key name as file. Remember this api only accepts json file."
        file = request.files['file']
        if file.filename == '':
            return "No file name found. Please try again."
        if file and allowed_file(file.filename):
            file.save('uploads/test.jpg')
            gender = detect_gender()
            return gender
        else:
            return "Wrong file type uploaded. This API only accepts json file correctly formatted."
    return "Some internal error occured"

if __name__ == '__main__':
    app.run(debug = False,threaded=False,host='0.0.0.0')




 