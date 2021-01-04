# VoPho: Voice command based attribute filtering android


VoPho application has three major parts, One andorid application and two classifiers running on Azure:

1. Application : Go into the folder VoPho for source code. Application is comaptible upto latest version of android. (May
not work below API version 15)
2. APK File : You can find apk file in apk folder named VoPho.apk
3. Gender Detection Service : Gender Detection service is running in Azure cloud server.
4. Indoor-Outdoor Service : Indoor-Outdoor Service is running in Azure cloude server. 
5. Postman : You can ping above provided two services out of android application as as well. If using Postman, Choose
POST as request type & key :“file”, key-type (on extreme right of key column) : “file” and choose file in Value
column.
6. Source Code : You can find source code of both services in filter folder in zip file.
7. test_images : We have uploaded a folder with test images as well for testing purpose. Although you could run this
application in any folder, we recommend using only clear face images with high resolution.

## Running Services locally

You need to install **Virtualenv or Pyenv**.

## Gender Detection Service

**Note : Python 3.7.x is a must requirement as application uses pandas subclass format which
requires 3.**

Install virtual environment thorugh pip in python 3.7.x.

```
pip3 install virtualenv
```
and create a virtual environment :

```
python3. 7 -m venv /path/to/new/virtual/environment
```
and run **req.txt** file which is present in **gender-detection folder in filter folder** to install all dependencies:


```
pip install -r req.txt
```
Run service using **webapp.py** which will run a flask server on **localhost:**

```
python webapp.py
```
You can ping service on **[http://localhost:5000/gender](http://localhost:5000/gender)**

### Indoor-Outdoor classification Service

**Note : Python 3.6.x is a must requirement as application uses keras/tensorflow model which
requires 3.**

create a virtual environment :

```
python3. 6 -m venv /path/to/new/virtual/environment
```
and run **req.txt** file which is present in **Indoor-Outdoor-scene-classification folder in filter folder** to install all
dependencies:

```
pip install -r req.txt
```
Run service using **webapp.py** which will run a flask server on **localhost:**

```
python webapp.py
```
You can ping service on **[http://localhost:5001/places](http://localhost:5001/places)**

## Train Models

**Note : Read "Gender Detection Service" subheading to create a virtualenv first, activate and
install requirements**
You can train gender-detection model yourself as well. For the purpose of project we trained over ~7000 images from
opensource database, **UTK Face in the Wild**. Follow steps to train model :

```
1. Put image files in any folder, for e.g we will take “part1”.
```

```
2. Images in above dataset is divided using second argument as gender. “1”: Woman, “0” : man. for e.g :
15_ 1 _4_20170104005807401.jpg is an image of woman.
3. To differentiate files in genders on provided train data, you can run diffrentiate.py file with proper arguments and it
will create a new folder with two subfolders, man and woman with respective images.
4. Now we are ready to run our classifier, just type python train.py -d {folder_name}.
5. You sill find plot.png as output image with loss , accuracy , val_acc and val_loss plotted.
```
## Folder Structure

```
Note : This is not a complete folder structure, just shows important files and directories.
```
```
project
│ README.pdf
└─── test_images
└───VoPho
│ └─── app/src/main
│ │ AndoridManifest.xml
│ │ └─── java/com/andoridcodeman/simpleimagegallery/
│ │ │ └─── fragments
│ │ │ └─── utils
│ │ │ ImageDisplay.java
│ │ │ MainActivity.java
│ │ └─── res
└─── filter
│ └─── gender-detection
│ │ └─── pre-trained
│ │ │ gender-detection.model
| | detect-gender.py
| | train.py
| | webapp.py
| | req.txt
│ └─── Indoor-Outdoor-scene-classification
| | whole_wideresnet18_places365_python36.pth.tar
| | run_placesCNN_unified.py
| | webapp.py
| | req.txt
```


