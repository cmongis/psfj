#!/bin/bash

FILES=./files
MACOS=./macos_str
VERSION=$(cat version)
BIN=../dist


if [[ -n "$1" ]]; then
	let "VERSION=VERSION+1";
fi
WL_NAME="psfj_2.5_build_$VERSION"
MAC_NAME="psfj_2.5_build_${VERSION}_macos"

echo "Building number $VERSION...";

# building windows/Linux bundle
echo $VERSION > version;
echo "Deleting build directory..."
rm -R build || echo "no build directory, creating"
mkdir build

#Create PSFj structure that will contains all the files for Windows and Linux
mkdir build/PSFj
echo "Creating WINDOWS/LINUX ZIP file"

#Copies the text files and executable
cp -R $FILES/* build/PSFj
cp -R $BIN/* build/PSFj

#Zips the whole things
zip -ur build/$WL_NAME.zip build/PSFj

#Copy it in the build directory
cp build/$WL_NAME.zip build/psfj_latest.zip

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
cd build
zip -ur ../$MAC_NAME.zip PSFj.app
cd ..
#going to the file folder
cd $FILES
zip -ur ../build/$MAC_NAME.zip *.txt;
zip -ur ../build/$MAC_NAME.zip *.pdf;


#copying and creating a new build
cp $MAC_NAME.zip build/psfj_macos_latest.zip

cd ..

#rm -R build/PSFj.app
