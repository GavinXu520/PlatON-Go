package kit

import (
	"encoding/hex"
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/common"
	"github.com/PlatONnetwork/PlatON-Go/core/rawdb"
	"github.com/PlatONnetwork/PlatON-Go/core/state"
	"github.com/PlatONnetwork/PlatON-Go/rlp"
	"github.com/PlatONnetwork/PlatON-Go/trie"
	"gopkg.in/urfave/cli.v1"
)

var (
	delAccountStateCmdFlags = []cli.Flag{
		dataDirFlag,
		blockNumFlag,
		accountFlag,
	}
	DelAccountState = cli.Command{
		Name:   "delaccountstate",
		Usage:  "delete account state",
		Action: delAccount,
		Flags:  delAccountStateCmdFlags,
	}
)

func delAccount(c *cli.Context) error {

	ldb := mustOpenDB(c.String(dataDirFlag.Name))
	addrHash := mustEncodeAddrHash(c.String(accountFlag.Name))
	number := c.Uint64(blockNumFlag.Name)
	Debug = c.Bool(debugFlag.Name)

	println("dir:", c.String(dataDirFlag.Name), "addr:", c.String(accountFlag.Name), "addrHash:", addrHash.String(), "number:", number)
	hash := rawdb.ReadCanonicalHash(ldb, number)
	println("block hash:", hash.String())
	num := rawdb.ReadHeaderNumber(ldb, hash)
	println("number:", *num)
	block := rawdb.ReadBlock(ldb, hash, number)
	if block == nil {
		return fmt.Errorf("find block num:%d is nil", number)
	}

	root := block.Root()
	tr, err := trie.NewSecure(root, trie.NewDatabase(ldb), 0)
	if err != nil {
		return err
	}

	var accountRoot common.Hash
	find := false
	iter := tr.NodeIterator(nil)
	// 遍历当前block 中的trie
	for iter.Next(true) {
		if iter.Leaf() {
			var obj state.Account
			err := rlp.DecodeBytes(iter.LeafBlob(), &obj)
			if err != nil {
				return fmt.Errorf("parse account error:%s", err.Error())
			}
			value := iter.LeafKey()
			println("account:", hex.EncodeToString(value), "nonce:", obj.Nonce)

			if hex.EncodeToString(value) == hex.EncodeToString(addrHash[:]) {
				println("find account ", "value:", hex.EncodeToString(value), "root:", obj.Root.String())
				accountRoot = obj.Root
				find = true
				break
			}
		}
	}

	// 如果在当前block 中找到了 该 acc 信息
	if find {
		println("find success account:", " root:", accountRoot.String())
		accountTrie, err := trie.NewSecure(accountRoot, trie.NewDatabase(ldb), 0)
		if err != nil {
			panic(fmt.Sprintf("open account err :%s", err.Error()))
		}
		println("account trie:", accountTrie.Hash().String())

		// 遍历 该Acc 的tire
		iter := accountTrie.NodeIterator(nil)
		for iter.Next(true) {
			if iter.Leaf() {

				var valueKey []byte
				if err := rlp.DecodeBytes(iter.LeafBlob(), &valueKey); err != nil {
					panic(err)
				}
				//secureKey := secureKey(valueKey)
				//value, err := ldb.Get(secureKey)

				// 删除该 key -> valueKey, valueKey -> value
				key := iter.LeafKey()
				if err = ldb.Delete(key); nil != err {
					fmt.Println("Failed to delele  key ", "key", hex.EncodeToString(key))
				}

				if err = ldb.Delete(valueKey); nil != err {
					fmt.Println("Failed to delele  valueKey ", "valueKey", hex.EncodeToString(valueKey))
				}

				/*value, err := ldb.Get(valueKey)
				if err != nil {
					fmt.Println("find value error key:", hex.EncodeToString(iter.LeafKey()), "valueKey:", hex.EncodeToString(valueKey), "error:", err.Error())
				} else {
					fmt.Println("key:", hex.EncodeToString(iter.LeafKey()), "valueKey:", hex.EncodeToString(valueKey), "value:", hex.EncodeToString(value))
				}*/
			}
		}
	} else {
		return fmt.Errorf("not found address : %s", c.String(accountFlag.Name))
	}

	return nil
}