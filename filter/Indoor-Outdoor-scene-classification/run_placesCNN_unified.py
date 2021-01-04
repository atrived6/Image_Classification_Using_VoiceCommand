import torch
from torch.autograd import Variable as V
import torchvision.models as models
from torchvision import transforms as trn
from torch.nn import functional as F
import os
import numpy as np
from scipy.misc import imresize as imresize
import cv2
from PIL import Image


def load_labels():
    file_name_IO = 'IO_places365.txt'
    with open(file_name_IO) as f:
        lines = f.readlines()
        labels_IO = []
        for line in lines:
            items = line.rstrip().split()
            labels_IO.append(int(items[-1]) -1) # 0 is indoor, 1 is outdoor
    labels_IO = np.array(labels_IO)

    return labels_IO

def hook_feature(module, input, output):
    features_blobs.append(np.squeeze(output.data.cpu().numpy()))


def returnTF():
    tf = trn.Compose([
        trn.Resize((224,224)),
        trn.ToTensor(),
        trn.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ])
    return tf


def load_model():

    model_file = 'whole_wideresnet18_places365_python36.pth.tar'
    useGPU = 0
    if useGPU == 1:
        model = torch.load(model_file)
    else:
        model = torch.load(model_file, map_location=lambda storage, loc: storage)

    model.eval()
    features_names = ['layer4','avgpool']
    for name in features_names:
        model._modules.get(name).register_forward_hook(hook_feature)
    return model
features_blobs = []
def  places():
    labels_IO = load_labels()
    
    model = load_model()
    tf = returnTF() 

    params = list(model.parameters())
    weight_softmax = params[-2].data.numpy()
    weight_softmax[weight_softmax<0] = 0

    img = Image.open('uploads/test.jpg')
    input_img = V(tf(img).unsqueeze(0), volatile=True)

    logit = model.forward(input_img)
    h_x = F.softmax(logit, 1).data.squeeze()
    probs, idx = h_x.sort(0, True)

    io_image = np.mean(labels_IO[idx[:10].numpy()]) # vote for the indoor or outdoor

    if io_image < 0.5:
        return "Indoor"
    else:
        return "Outdoor"
