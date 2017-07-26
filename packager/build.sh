#!/bin/bash

FILES=./files
MACOS=./macos_str
VERSION=$(cat version)
BIN=../dist
BUILD_FOLDER=build

if [[ -n "$1" ]]; then
	let "VERSION=VERSION+1";
fi
WL_NAME="psfj_2.5_build_$VERSION"
MAC_NAME="psfj_2.5_build_${VERSION}_macos"

echo "Building number $VERSION...";

# building windows/Linux bundle
echo $VERSION > version;
echo "Deleting build directory..."
rm -R $BUILD_FOLDER || echo "no build directory, creating"
mkdir $BUILD_FOLDER

#Create PSFj structure that will contains all the files for Windows and Linux
mkdir $BUILD_FOLDER/PSFj
echo "Creating WINDOWS/LINUX ZIP file"

#Copies the text files and executable
cp -R $FILES/* $BUILD_FOLDER/PSFj
cp -R $BIN/* $BUILD_FOLDER/PSFj

cd $BUILD_FOLDER

#Zips the whole things
zip -ur $WL_NAME.zip ./PSFj

#Copy it in the build directory
cp ./$WL_NAME.zip ./psfj_latest.zip

cd ../
#Delete the build
#rm -R build/PSFj


# build MacOS Version
echo "Creating MAC OS structure"

#this folder contains the macos structure
cp -Rv macos_str build/PSFj.app

#copying the binaries in the good folder
cp -Rv $BIN/* build/PSFj.app/Contents/MacOS/

#Copying licence and readme
cp -Rv $FILES/*.txt build/PSFj.app/Contents/MacOS/

#copying the execution script
cp -Rv $FILES/psfj build/PSFj.app/Contents/MacOS/

echo "Zipping MacOS Bundle"
#going to the build folder to get the zip hierarchy right
cd $BUILD_FOLDER
zip -ur ../$BUILD_FOLDER/$MAC_NAME.zip PSFj.app
cd ..
#going to the file folder
cd $FILES
zip -ur ../$BUILD_FOLDER/$MAC_NAME.zip *.txt;
zip -ur ../$BUILD_FOLDER/$MAC_NAME.zip *.pdf;


#copying and creating a new build
cp ../$BUILD_FOLDER/$MAC_NAME.zip ../$BUILD_FOLDER/psfj_macos_latest.zip

cd ..

#rm -R build/PSFj.app
