package main

import (
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/core/rawdb"
	"github.com/PlatONnetwork/PlatON-Go/ethdb"
	"github.com/PlatONnetwork/PlatON-Go/rlp"
	"os"
	"strconv"
)

func main() {
	ldb := mustOpenDB(os.Args[1])
	n, err := strconv.Atoi(os.Args[2])
	if err != nil {
		panic(err)
	}
	number := uint64(n)
	hash := rawdb.ReadCanonicalHash(ldb, number)
	block := rawdb.ReadBlock(ldb, hash, number)

	hbuf, err := rlp.EncodeToBytes(block.Header())
	if err != nil {
		panic(err)
	}
	buf, err := rlp.EncodeToBytes(block)
	if err != nil {
		panic(err)
	}
	if block != nil {
		fmt.Println(fmt.Sprintf("block size:%d, tx:%d extradata:%d, header:%d avg:%d", len(buf), len(block.Transactions()), len(block.ExtraData()), len(hbuf), (len(buf)-len(block.ExtraData())-len(hbuf))/len(block.Transactions())))
	}

}

func mustOpenDB(dir string) *ethdb.LDBDatabase {
	ldb, err := ethdb.NewLDBDatabase(dir, 0, 1)
	if err != nil {
		panic(err)
	}
	return ldb
}
