from keras.preprocessing.image import img_to_array
from keras.models import load_model
from keras.utils import get_file
import numpy as np
import argparse
import cv2
import os
import cvlib as cv
import threading

model_path = "./pre-trained/gender_detection.model"
model = load_model(model_path)
def detect_gender():
    test_var=threading.local()
    test_var.__setattr__('value',"value")
    
    image = cv2.imread('uploads/test.jpg')

    if image is None:
        print("Could not read input image")
        exit()
    # detect faces in the image
    face, confidence = cv.detect_face(image)

    if(len(face)==0):
        return "No face in image"

    classes = ['man','woman']
    try:
        for idx, f in enumerate(face):

            # get corner points of face rectangle       
            (startX, startY) = f[0], f[1]
            (endX, endY) = f[2], f[3]
            # draw rectangle over face
            cv2.rectangle(image, (startX,startY), (endX,endY), (0,255,0), 2)

            # crop the detected face region
            face_crop = np.copy(image[startY:endY,startX:endX])

            # preprocessing for gender detection model
            face_crop = cv2.resize(face_crop, (96,96))
            face_crop = face_crop.astype("float") / 255.0
            face_crop = img_to_array(face_crop)
            face_crop = np.expand_dims(face_crop, axis=0)

            # apply gender detection on face
            conf = model.predict(face_crop)[0]
            print(conf)
            print(classes)
            if(conf[0]>conf[1]):
                return "man"
            elif(conf[1]>conf[0]):
                return "Woman"
            else:
                return "No gender detected"
    except:
        return "No gender detected"
    # loop through detected faces
    