package kits

import (
	"flag"
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/ethdb"
	"gopkg.in/urfave/cli.v1"
)

var (
	ScanImage = cli.Command{
		Name:   "scan_image",
		Usage:  "scan image size",
		Action: scanImage,
		Flags: []cli.Flag{
			dbFlag,
		},
	}
)

func scanImage(c *cli.Context) error {
	flag.Parse()

	dbPath := c.String(dbFlag.Name)

	ldb, err := ethdb.NewLDBDatabase(dbPath, 0, 1)
	if err != nil {
		panic(err)
	}

	iterator := ldb.NewIteratorWithPrefix([]byte("secure-key-"))
	imageTotal := int64(0)
	keyTotal := int64(0)
	for iterator.Next() {
		imageTotal += int64(len(iterator.Key()) + len(iterator.Value()))
		keyTotal++
	}
	fmt.Println("image", Mb(imageTotal), "key", keyTotal)
	return nil
}
