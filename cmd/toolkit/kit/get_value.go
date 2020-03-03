package kit

import (
	"encoding/hex"
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/common"
	"gopkg.in/urfave/cli.v1"
)

var (
	GetValueCmd = cli.Command{
		Name:     "getvalue",
		Usage:    "get value",
		Action:   getValue,
		Flags:    getValueFlags,
		HelpName: "wo",
	}
	keyFlag = cli.StringFlag{
		Name:  "key",
		Usage: "key",
	}

	getValueFlags = []cli.Flag{
		dataDirFlag,
		keyFlag,
	}
)

func getValue(c *cli.Context) error {
	ldb := mustOpenDB(c.String(dataDirFlag.Name))
	hash := common.HexToHash(c.String(keyFlag.Name))
	res, err := ldb.Get(secureKey(hash[:]))
	if err != nil {
		return err
	}

	fmt.Println(hex.EncodeToString(res))
	return nil
}
