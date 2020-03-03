package kits

import (
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/common"
	"github.com/PlatONnetwork/PlatON-Go/core/rawdb"
	"github.com/PlatONnetwork/PlatON-Go/core/state"
	"github.com/PlatONnetwork/PlatON-Go/ethdb"
	"github.com/PlatONnetwork/PlatON-Go/rlp"
	"github.com/PlatONnetwork/PlatON-Go/trie"
	"gopkg.in/urfave/cli.v1"
)

var (
	ScanMerkle = cli.Command{
		Name:   "scan_merkle",
		Usage:  "scan merkle size",
		Action: scanMerkle,
		Flags: []cli.Flag{
			dbFlag,
			numFlag,
		},
	}
)

func scanMerkle(c *cli.Context) error {

	dbPath := c.String(dbFlag.Name)
	number := c.Uint64(numFlag.Name)

	ldb, err := ethdb.NewLDBDatabase(dbPath, 0, 1)
	if err != nil {
		panic(err)
	}
	total := int64(0)

	if number != 0 {
		total = scanBlockMerkle(ldb, number)
	} else {
		empty := common.Hash{}
		for {
			hash := rawdb.ReadCanonicalHash(ldb, number)
			if hash == empty {
				break
			}
			total += scanBlockMerkle(ldb, number)
			number++
		}
	}

	fmt.Println("number", number, "total", Mb(total))
	return nil
}

func scanBlockMerkle(ldb *ethdb.LDBDatabase, number uint64) int64 {
	hash := rawdb.ReadCanonicalHash(ldb, number)
	//num := rawdb.ReadHeaderNumber(ldb, hash)

	block := rawdb.ReadBlock(ldb, hash, number)

	root := block.Root()
	//fmt.Println("block root", root.String())
	triedb := trie.NewDatabase(ldb)
	tr, err := trie.NewSecure(root, triedb, 0)
	if err != nil {
		panic(err)
	}
	total := int64(0)
	iter := tr.NodeIterator(nil)
	for iter.Next(true) {
		//fmt.Println("hash:", iter.Hash())
		if b, err := ldb.Get(iter.Hash().Bytes()); err == nil {
			total += int64(len(b) + 32)
		}
		if iter.Leaf() {
			var obj state.Account
			err := rlp.DecodeBytes(iter.LeafBlob(), &obj)
			if err != nil {
				panic(err)
			}
			//value := iter.LeafKey()

			total += scanAccount(ldb, obj.Root)
			//println("account:", hex.EncodeToString(value), "nonce:", obj.Nonce, "total", total)

		}
	}
	fmt.Println("block", number, "merkle size", Mb(total))
	return total
}
func scanAccount(ldb *ethdb.LDBDatabase, root common.Hash) int64 {
	tr, err := trie.NewSecure(root, trie.NewDatabase(ldb), 0)
	if err != nil {
		panic(err)
	}
	total := int64(0)

	iter := tr.NodeIterator(nil)
	for iter.Next(true) {
		if b, err := ldb.Get(iter.Hash().Bytes()); err != nil {
			total += int64(len(b) + 32)
		}
	}
	return total
}
