import os
import shutil
entries = os.listdir('part1/')
dir_w = "woman/"
dir_m = "man/"
for f in entries:
    if(f.split("_")[1]=="1"):
        shutil.copy("part1/"+f, dir_w)
    else:
        shutil.copy("part1/"+f, dir_m)
