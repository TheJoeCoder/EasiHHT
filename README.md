# EasiHHT
A Java app to do an Easitill stock take interactively on any Java-supporting device, no expensive terminal required!

I am planning on adding a GUI/making an Android app soon, but for now it's a console-only app (although it can be run on Android via Termux).

## Why?
I made this because Hand-Held Terminals are expensive, both to rent and to buy outright. The only one compatible with Easitill (as far as I know), the discontinued Nordic RF601, easily goes for £600 or more to buy and £250 to rent for a week. 
So, we all have a phone or laptop lying around, why not use it?

## How to
These instructions allow you to zero the stock list and complete a stock take.
* Export the `ProductData.csv` file from the master till. This can be done by going to Tools -> Export Internet Data in the Easitill client/supervisor. Be sure to deselect "Ecommerce items only" and to set File Format to CSV.
* Download the JAR from the releases page (or compile it yourself)
* Open a command prompt or terminal and run `java -jar easihht.jar ProductData.csv --zero` (assuming both the jar and the `ProductData.csv` file are in your working directory).
* Connect a barcode scanner, and you're good to go!
* Once finished, type "q" to quit the app, and you should have a `stock.csv` file which you can import*.

(*Note: I still haven't tested importing back to a till, so I don't have clear instructions as of how to do it yet. I will update this page when I know how to do this.)

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