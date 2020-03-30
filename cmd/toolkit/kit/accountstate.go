package kit

import (
	"encoding/hex"
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/common"
	"github.com/PlatONnetwork/PlatON-Go/core/rawdb"
	"github.com/PlatONnetwork/PlatON-Go/core/state"
	"github.com/PlatONnetwork/PlatON-Go/crypto"
	"github.com/PlatONnetwork/PlatON-Go/rlp"
	"github.com/PlatONnetwork/PlatON-Go/trie"
	"gopkg.in/urfave/cli.v1"
	"time"
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
	println("accountstate block hash:", hash.String())
	block := rawdb.ReadBlock(ldb, hash, number)
	if block == nil {
		return fmt.Errorf("find block num:%d is nil", number)
	}

	root := block.Root()
	tr, err := trie.NewSecure(root, trie.NewDatabase(ldb), 0)
	if err != nil {
		fmt.Println("accountstate new trie is failed", "blockNumber", number, "blockHash", hash.String(), "root", root.Hex(), "err", err)
		return err
	}

	// c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470
	emptyCodeHash := crypto.Keccak256(nil)
	fmt.Println("accountstate emptyHashStr", hex.EncodeToString(emptyCodeHash), "\naccountstate emptyHashHex", common.BytesToHash(emptyCodeHash).Hex())


	emptyHashStr := hex.EncodeToString(emptyCodeHash)

	totalAccountCount := 0
	contractAccountCount := 0

	start := time.Now()


	stats := make(map[string]int, 0)

	rootHash := make(map[string]struct{}, 0)
	//
	//defeRootHash := make(map[string][]string, 0)


	addrs :=  make(map[string]struct{}, 0)


	iter := tr.NodeIterator(nil)
	for iter.Next(true) {
		if iter.Leaf() {
			var obj state.Account
			err := rlp.DecodeBytes(iter.LeafBlob(), &obj)
			if err != nil {
				return fmt.Errorf("parse account error:%s", err.Error())
			}

			if hex.EncodeToString(obj.CodeHash) != emptyHashStr && "737e0f5e7391bac57b4213527b5a343f732279df47448657afac9980719ae28f" != hex.EncodeToString(obj.CodeHash) {
				value := iter.LeafKey()

				addrs[hex.EncodeToString(value)] = struct{}{}
				fmt.Println("accountAddr Hash:",/* common.BytesToHash(value).String(),*/ hex.EncodeToString(value), "nonce:", obj.Nonce, "codehash", hex.EncodeToString(obj.CodeHash))
				contractAccountCount++

				codeHash := hex.EncodeToString(obj.CodeHash)
				if c, ok := stats[codeHash]; ok {
					c++
					stats[codeHash] = c
				} else {
					stats[codeHash] = 1
				}

				crh := codeHash+"_" + obj.Root.Hex()

				if _, ok := rootHash[crh]; !ok {
					rootHash[crh] = struct{}{}
				}



				//if r, ok := rootHash[codeHash]; !ok {
				//	rootHash[codeHash] = obj.Root.Hex()
				//	defeRootHash[codeHash] = []string{obj.Root.Hex()}
				//} else {
				//	if r != obj.Root.Hex() {
				//		arr := defeRootHash[codeHash]
				//		arr = append(arr, obj.Root.Hex())
				//	}
				//}
			}

			//fmt.Println("accountAddr Hash:", hex.EncodeToString(value), "nonce:", obj.Nonce, "codehash", hex.EncodeToString(obj.CodeHash))

			totalAccountCount++

		}
	}

	duration := time.Since(start)
	fmt.Println("using time duration:", duration)
	fmt.Println("the contract account count:", contractAccountCount)
	fmt.Println("the total account count:", totalAccountCount)

	for k, v := range stats {
		//arr := defeRootHash[k]
		fmt.Println(k, v)
	}

	for addrHash, _ := range addrs {
		fmt.Println("addrHash: ", addrHash)
	}

	//for k, _ := range rootHash {
	//	fmt.Println(k)
	//}

	//fmt.Println("rootHash", len(rootHash))

	return nil
}
