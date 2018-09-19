package helpers

import (
	"fmt"
	"globals"
)

func HandleReturn() {
	if globals.ExitError != nil {
		fmt.Printf("Exited with code %d and Reason: %s", globals.ExitCode, globals.ExitError)
		return
	}
}
