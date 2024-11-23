# WorkTime
[![Build Status](https://github.com/Keidan/WorkTime/actions/workflows/build.yml/badge.svg)][build]
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)][license]

(GPL) Android Work time management is a FREE software.

This application allow me to manage and report my work time.


## Instructions


download the software :

	mkdir devel
	cd devel
	git clone git@github.com:Keidan/WorkTime
	cd WorkTime
 	Use with android studio

	
## Dropbox

:warning: To import/export the database file using the dropbox cloud, you need to follow the steps below:
* [Create a dropbox application](https://www.dropbox.com/developers/apps)
* Edit the file [WorkTime/app/src/main/AndroidManifest.xml](https://github.com/Keidan/WorkTime/blob/master/app/src/main/AndroidManifest.xml) and replace `YOUR_APP_KEY_HERE` by your application key
* Edit the file [WorkTime/app/src/main/res/values/strings.xml](https://github.com/Keidan/WorkTime/blob/master/app/src/main/res/values/strings.xml) and replace `YOUR_APP_KEY_HERE` by your application key


## License

[GPLv3](https://github.com/Keidan/WorkTime/blob/master/license.txt)

[build]: https://github.com/Keidan/WorkTime/actions
[license]: https://github.com/Keidan/HexViewer/blob/master/license.txt