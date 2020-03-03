package kits

import (
	"fmt"
	"gopkg.in/urfave/cli.v1"
)

var (
	dbFlag = cli.StringFlag{
		Name:  "db",
		Usage: "chaindata dir",
	}

	numFlag = cli.Uint64Flag{
		Name:  "num",
		Usage: "block number",
		Value: 0,
	}
)

func Mb(size int64) string {
	return fmt.Sprintf("%f Mb", float64(size)/1024/1024)
}
