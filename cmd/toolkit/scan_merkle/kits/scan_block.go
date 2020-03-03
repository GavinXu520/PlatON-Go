package kits

import (
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/common"
	"github.com/PlatONnetwork/PlatON-Go/core/rawdb"
	"github.com/PlatONnetwork/PlatON-Go/core/types"
	"github.com/PlatONnetwork/PlatON-Go/ethdb"
	"github.com/PlatONnetwork/PlatON-Go/rlp"
	"gopkg.in/urfave/cli.v1"
)

var (
	ScanBlock = cli.Command{
		Name:   "scan_block",
		Usage:  "scan block size",
		Action: scanBlock,
		Flags: []cli.Flag{
			dbFlag,
		},
	}
)

func scanBlock(c *cli.Context) error {

	dbPath := c.String(dbFlag.Name)

	ldb, err := ethdb.NewLDBDatabase(dbPath, 0, 1)
	if err != nil {
		panic(err)
	}

	receiptTotal := int64(0)
	headerTotal := int64(0)
	bodyTotal := int64(0)

	num := uint64(0)

	empty := common.Hash{}
	for {
		hash := rawdb.ReadCanonicalHash(ldb, num)
		if hash == empty {
			break
		}

		body := rawdb.ReadBodyRLP(ldb, hash, num)
		header := rawdb.ReadHeaderRLP(ldb, hash, num)
		receipts := rawdb.ReadReceipts(ldb, hash, num)

		storageReceipts := make([]*types.ReceiptForStorage, len(receipts))
		for i, receipt := range receipts {
			storageReceipts[i] = (*types.ReceiptForStorage)(receipt)
		}
		bytes, _ := rlp.EncodeToBytes(storageReceipts)
		receiptTotal += int64(len(bytes))
		bodyTotal += int64(len(body))
		headerTotal += int64(len(header))
		num++
	}

	fmt.Println("number", num, "header", Mb(headerTotal), "body", Mb(bodyTotal), "receipt", Mb(receiptTotal), "block", Mb(headerTotal+bodyTotal))

	return nil
}
