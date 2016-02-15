## Setting the Operating system
ifeq ($(OS),Windows_NT)
    OS_detected := Windows
else
    OS_detected := $(shell uname -s)
endif

echo:
	@echo "OS version detected as $(OS_detected)"

create-db:
	@echo "When it starts to work this command will create the database schema in the present machine"

run:
	lein uberjar
	java -jar ./target/uberjar/lunchselector-0.1.0-SNAPSHOT-standalone.jar &
