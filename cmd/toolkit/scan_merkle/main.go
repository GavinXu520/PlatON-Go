package main

import (
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/cmd/toolkit/scan_merkle/kits"
	"github.com/PlatONnetwork/PlatON-Go/cmd/utils"
	"gopkg.in/urfave/cli.v1"
	"os"
	"sort"
)

var (
	app = utils.NewApp("", "scan storage")
)

func init() {

	// Initialize the CLI app
	app.Commands = []cli.Command{
		kits.ScanMerkle,
		kits.ScanImage,
		kits.ScanBlock,
	}
	sort.Sort(cli.CommandsByName(app.Commands))
	app.After = func(ctx *cli.Context) error {
		return nil
	}
}

func main() {
	if err := app.Run(os.Args); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
