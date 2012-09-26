# jmandle
#### Java Manga to Kindle Image Converter

### Overview

The purpose of this project is to provide a cross-platform, simple application that enables the conversion of
scanned manga images to a format optimized for reading on a Kindle device. While such application do exist, the
most notable of those are only available for the Microsoft Windows platform.


### Requirements

To be able to successfully build the application, you will need to include the brilliant
[Marvin Image Processing Framework](http://marvinproject.sourceforge.net).


### Features

The following features are currently available:

  * Conversion to gray scale
    * Optional usage of only 16 shades
    * Optional removing of background noise
  * Cropping of white borders
  * Splitting of landscape images
    * Left to right or right to left
  * Scaling of images
    * Kindle 2, 3, 4, 5 - Kindle DX - Kindle Paperwhite
    * Optional preservation of the image aspect ratio
    * Optional adding of white borders to center image
  * Optional multi-threaded processing