package kit

import (
	"encoding/hex"
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/core/rawdb"
	"github.com/PlatONnetwork/PlatON-Go/core/state"
	"github.com/PlatONnetwork/PlatON-Go/rlp"
	"github.com/PlatONnetwork/PlatON-Go/trie"
	"gopkg.in/urfave/cli.v1"
)

var (
	accountStateCmdFlags = []cli.Flag{
		dataDirFlag,
		blockNumFlag,
	}
	AccountState = cli.Command{
		Name:   "accountstate",
		Usage:  "account state",
		Action: accountState,
		Flags:  accountStateCmdFlags,
	}
)

func accountState(c *cli.Context) error {
	ldb := mustOpenDB(c.String(dataDirFlag.Name))
	number := c.Uint64(blockNumFlag.Name)

	hash := rawdb.ReadCanonicalHash(ldb, number)
	println("block hash:", hash.String())
	block := rawdb.ReadBlock(ldb, hash, number)
	if block == nil {
		return fmt.Errorf("find block num:%d is nil", number)
	}

	root := block.Root()
	tr, err := trie.NewSecure(root, trie.NewDatabase(ldb), 0)
	if err != nil {
		return err
	}

	iter := tr.NodeIterator(nil)
	for iter.Next(true) {
		if iter.Leaf() {
			var obj state.Account
			err := rlp.DecodeBytes(iter.LeafBlob(), &obj)
			if err != nil {
				return fmt.Errorf("parse account error:%s", err.Error())
			}
			value := iter.LeafKey()
			fmt.Println("account:", hex.EncodeToString(value), "nonce:", obj.Nonce, "codehash", hex.EncodeToString(obj.CodeHash))

		}
	}
	return nil
}
