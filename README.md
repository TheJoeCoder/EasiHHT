# EasiHHT
A Java app to do an Easitill stock take interactively on any Java-supporting device, no expensive terminal required!

I am planning on adding a GUI/making an Android app soon, but for now it's a console-only app (although it can be run on Android via Termux).

## Why?
I made this because Hand-Held Terminals are expensive, both to rent and to buy outright. The only one compatible with Easitill (as far as I know), the discontinued Nordic RF601, easily goes for £600 or more to buy and £250 to rent for a week. 
So, we all have a phone or laptop lying around, why not use it?

## How to
These instructions allow you to zero the stock list and complete a stock take. Please note this app requires Java to be installed on your device.
### Preparation
* Export the `ProductData.csv` file from the master till. This can be done by going to Tools -> Export Internet Data in the Easitill client/supervisor. Be sure to deselect "Ecommerce items only" and to set File Format to CSV.
* Move the `ProductData.csv` file to a folder on the device you are running the stock take on.
* Download `easihht.jar` [here](https://github.com/TheJoeCoder/EasiHHT/releases/latest/download/easihht.jar) (or compile it yourself) and move it to the same folder you moved the `ProductData.csv` file to.
* Open a command prompt or terminal in the directory where ProductData.csv and `easihht.jar` are located.
### Doing the stock take
There are multiple methods you can use to complete the stock take outlined below. For more details see the command line options section.
#### Update the stock (not zeroing records)
`java -jar easihht.jar ProductData.csv`
#### Complete stock take (setting all records to zero stock)
`java -jar easihht.jar ProductData.csv -z`
#### Resume from a previous stock take
`java -jar easihht.jar ProductData.csv -r stock.csv`
### Finishing the stock take
* Once finished, type "q" to quit the app, and you should have a `stock.csv` file.
* Transfer this file to the master till and open Easitill Supervisor.
* Make a system backup if you haven't already by going to Tools -> Backup System Data.
* Go to Data -> Stock Data Import and press Yes confirming you've done a backup.
* Select the `stock.csv` file.
* Press the Setup button in the bottom middle. 
* Select column 1, set it to "Linecode" and check "Key Field". Press OK.
* Select column 2, set it to "Stock Quantity" and check "Update Field". Press OK.
* Press Cancel to exit the setup.
* At the bottom left, deselect "Add Record If Does Not Exist", select "Update Record If Exists". Deselect "Prompt Before Write" if you're doing a large-scale stock take and do not want to check each item.
* Press Import. If you kept "Prompt Before Write" activated, you will be asked to accept or reject the changes.
* Stock take done!

## Command Line Options
```
Usage: java -jar easihht.jar
                <file> [-z|--zero] [(-r|--resume) <resumetake>] [(-o|--output) <outputfile>]

  <file>
        ProductData.csv file to import

  [-z|--zero]
        Zero stock after importing

  [(-r|--resume) <resumetake>]
        Continue stock take from specified stock.csv file (overrides -z)

  [(-o|--output) <outputfile>]
        Set output file
```

## License
This software is governed by the GNU General Public License V3. Refer to the `LICENSE` file in this repository for more information.

To summarise:
* There is no warranty for using this software. If you break anything while using this software, I am not responsible. You use this software at your own risk. Please make a till backup before doing anything else.
* You may use the software for commercial purposes, but if you include it as a part of another software, you must release the source code of your software under the same license.

## Trademark Notice
Easitill is a trademark of Easitill Ltd. Their name and work remains copyrighted by them.
This software is an open-source utility program made by me (**not** made or endorsed by Easitill), capable of reading their export formats.

## Software Notes
* This software deals with stock amounts as whole numbers (integers). If, for some reason, you require partial stock amounts, then this will not work for you.