package main

import (
	"cli"
	"client"
	"constants"
	"globals"
	"helpers"
)

func main() {
	defer helpers.HandleReturn()
	// Read from command line arguments
	if err := cli.ParseFlags(); err != nil {
		globals.ExitError = err
		globals.ExitCode = constants.InvalidCommandLineArgumentExit
		return
	}
	// Use the read command line arguments to formulate the request
	if err := client.ComposeAndSendTransaction(); err != nil {
		globals.ExitError = err
		globals.ExitCode = constants.UnableToComposeTransactionExit
		return
	}
}
