# prpi

Projet Réseau - Plugin IntelliJ


## How to suppress log4j configuration warning on IntelliJ startup

!! This section is not needed anymore - Skip it !!

~~For IntelliJ :~~

~~Run > Edit Configurations~~
~~In the run configuration for the PrPi module, add in VM Options :~~
* ~~`-Dlog4j.configuration=file:C:\Users\David\Documents\INSA\4IF\ReseauSecurite\prpi\resources\log4j.xml` for Windows~~
* ~~`-Dlog4j.configuration=file:/home/david/Documents/INSA/4IF/ReseauSecurite/prpi/resources/log4j.xml` for a UNIX System~~
