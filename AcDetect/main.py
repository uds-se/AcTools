import glob
import xml.etree.ElementTree as ET
import os
import shutil
import sys

EditText = "EditText"
xmlns = "{http://schemas.android.com/apk/res/android}"
password = "Password"
accessiblity = xmlns + "importantForAccessibility"


appName = sys.argv[1]
if ".apk" in appName:
    appName = appName.replace('.apk', '')

if not os.path.exists(appName):
        os.system("apktool d " + appName + ".apk >/dev/null 2>&1")

xmlFiles = glob.glob(appName + "/res/layout/*.xml")

def checkFile(xmlFile):
    tree = ET.parse(xmlFile)
    root = tree.getroot()
    if (traverseTree(root) == True):
        print(xmlFile)

tempArray = []

def traverseTree(root):
    if EditText in root.tag:
        try:
            if (xmlns + "inputType") in root.attrib.keys():
                if password in root.attrib[xmlns + "inputType"]:
                    if (accessiblity in root.attrib.keys() and root.attrib[accessiblity] not in ["no",
                                                                                                 "noHideDescendants"]):
                        tempArray.append("Warning")
                    if accessiblity not in root.attrib.keys():
                        tempArray.append("Warning")
        except:
            # print("exception")
            tempArray.append("Exception")

    for elem in root.getchildren():
        if (traverseTree(elem) == True):
            return True

for xmlFile in xmlFiles:
    checkFile(xmlFile)


if "Exception" in tempArray:
    print("There was a problem in reading the file")
elif "Warning" in tempArray:
    print("The file has the accessibility vulnerability")
else:
    print("The file does not have the accessibility vulnerability")

shutil.rmtree(appName, ignore_errors=True)